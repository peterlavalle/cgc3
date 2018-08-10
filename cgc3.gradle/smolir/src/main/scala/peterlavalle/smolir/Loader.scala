package peterlavalle.smolir

import java.io.Writer

import peterlavalle.TS
import SmolIr.TCall

class Loader(m: SmolIr.Module) {
	implicit val module: SmolIr.Module = m
	val examine: Examine = new Examine(module)

	val moduleName: String = module.name.text
	val prefixName: String = module.prefix.text

	def apply[W <: Writer](writer: W): W = {
		import Examine._
		writer
			.appund {
				"\n#include \"" + moduleName + ".hpp\"\n\n"
			}
			.appund {
				s"""
					 |void smol_code(size_t count, const char* prefix, void** ptr, void* userdata, void*(*callback)(void*, const char*), const char* allnames);
					 |
					 |$moduleName _$moduleName;
					 |
					 |void $moduleName::def(void* userdata, void*(*callback)(void*, const char*), const char* allnames)
					 |{
					 |	smol_code(
					 |		${examine.allCallable.distinctBy(_.name.text).size},
					 |		"$prefixName",
					 |		reinterpret_cast<void**>(&(_$moduleName)),
					 |		userdata,
					 |		callback,
					 |		allnames
					 |	);
					 |}
					 |
					 |#include <assert.h>
					 |#ifdef WIN32
					 |#	define SMOL_CALL __stdcall*
					 |#else
					 |#	define SMOL_CALL *
					 |#endif
					 |
					""".stripTrim
			}
			.appund(examine.allCallableGroups.toList.sortBy((_: (String, Iterable[SmolIr.TCall]))._1)
				// repack the stream to include the group name and the object type
				.flatMap {
				case (g, d) =>
					val calls = d.toList.sortBy((_: TCall).name.text)
					d match {

						case SmolIr.Member(self, _) :: _ =>
							g :: self :: calls

						case _ =>
							g :: calls
					}
			}.filter("" != (_: Object))) {

				case group: String =>
					s"\n\n//\n// $group\n"

				case hard: SmolIr.TypeDef if hard.isHard =>
					val name = hard.name.text
					val kind = moduleName + "::" + name
					val base = {
						hard.value.text.drop(2).dropRight(2)
					}
					s"""
						 |$kind::$name(${Cpp.textForKind(hard.base)} _) :
						 |	_this(_)
						 |{}
						 |$kind::$name(void) :
						 |	_this($base)
						 |{}
						 |$kind::$name($kind&& them) :
						 |	_this(them._this)
						 |{
						 |	them._this = $base;
						 |}
						 |void $kind::operator=($kind&& them)
						 |{
						 |	this->~$name();
						 |	_this = them._this;
						 |	them._this = $base;
						 |}
					""".stripMargin.trim + '\n'

				case soft: SmolIr.TypeDef =>
					assume(!soft.isHard)
					""

				case callable: SmolIr.TCall =>
					callable match {
						case SmolIr.Prototype(code, name, args, kind) =>
							val call: String =
								reinterpretFunctionPointer(
									name,
									"", SmolIr.KVoid,
									args,
									kind
								)

							val (pre: String, end: String) =
								result(kind)

							val head: String =
								s"${Cpp.textForKind(kind)} $moduleName::${code.text}(${Header.argsToString(args)})"

							emitCallable(
								callable,
								head,
								pre, call, end
							)

						case SmolIr.Member(typeDef: SmolIr.TypeDef, SmolIr.TypeDef.Constructor(name: TS.Tok, args: SmolIr.TCall.Args)) =>
							import Examine._
							val hasThis =
								args.contains(SmolIr.TCall.ThisArg)

							val call: String =
								if (hasThis)
									reinterpretFunctionPointer(
										name,
										"&(_this._this)", typeDef.base.Ptr,
										args, SmolIr.KVoid
									)
								else
									reinterpretFunctionPointer(
										name,
										"", SmolIr.KVoid,
										args,
										typeDef.base
									)

							def out = {
								assume(typeDef.isHard)
								s"${Cpp.textForKind(typeDef)} _this;\n\t"
							}

							val (pre: String, end: String) =
								if (hasThis) {
									assume(typeDef.isHard)
									(out, ";\n\treturn _this")
								} else if (typeDef.isHard)
									(out + "_this._this = ", ";\n\treturn _this")
								else
									("_this = ", "")

							val head: String =
								if (typeDef.isHard)
									s"${Cpp.textForKind(typeDef)} ${Cpp.textForKind(typeDef)}::${name.text}(${Header.argsToString(args.filterTo[SmolIr.TCall.Arg])})"
								else
									s"${Cpp.textForKind(typeDef)}::${typeDef.name.text}(${Header.argsToString(args.filterTo[SmolIr.TCall.Arg])})"

							emitCallable(
								callable,
								head,
								pre, call, end
							)

						case SmolIr.Member(typeDef: SmolIr.TypeDef, SmolIr.TypeDef.Method(code: TS.Tok, name: TS.Tok, args: SmolIr.TCall.Args, kind: SmolIr.TKind)) =>
							val call: String =
								reinterpretFunctionPointer(
									name,
									"_this", typeDef.base,
									args,
									kind
								)

							val (pre: String, end: String) =
								result(kind)

							val head: String =
								s"${Cpp.textForKind(kind)} $moduleName::${typeDef.name.text}::${code.text}(${Header.argsToString(args.filterNot(SmolIr.TCall.ThisArg == _))})"

							emitCallable(
								callable,
								head,
								if (typeDef.isHard)
									s"${typeDef.hardCheck("_this").trim}\n\t$pre"
								else
									pre, call, end
							)

						case SmolIr.Member(typeDef: SmolIr.TypeDef, SmolIr.TypeDef.Destructor(name: TS.Tok, args)) =>
							val call: String =
								if (args.contains(SmolIr.TCall.ThisArg))
									reinterpretFunctionPointer(
										name,
										"&(_this)", typeDef.base.Ptr,
										args,
										SmolIr.KVoid
									)
								else {
									assume(args.isEmpty)
									reinterpretFunctionPointer(
										name,
										"_this", typeDef.base,
										if (args.isEmpty)
											List(SmolIr.TCall.ThisArg)
										else
											args,

										SmolIr.KVoid
									)
								}

							val (pre: String, end: String) =
								(s"if (_this == (${typeDef.value.text.drop(2).dropRight(2)}))\n\t\treturn;\n\t", "")

							emitCallable(
								callable,
								s"$moduleName::${typeDef.name.text}::~${typeDef.name.text}(void)",
								pre, call, end
							)

					}

			}
	}

	def result(kind: SmolIr.TKind): (String, String) =
		kind match {
			case SmolIr.KVoid =>
				("", "")
			case _: SmolIr.TypeDef =>
				(s"return ${Cpp.textForKind(kind)}(", ")")
			case _ =>
				("return ", "")
		}

	def reinterpretFunctionPointer
	(
		name: TS.Tok,
		selfData: TS.Tok,
		selfKind: SmolIr.TKind,
		args: SmolIr.TCall.Args,
		output: SmolIr.TKind
	): String = {

		val fullArgs: SmolIr.TCall.Args =
			args

		val castKind: String = {
			import Examine._
			val castArgs: String =
				if (fullArgs.nonEmpty)
					fullArgs.map {
						case SmolIr.TCall.ThisArg =>
							selfKind
						case SmolIr.TCall.Arg(_: TS.Tok, hard: SmolIr.TypeDef) if hard.isHard =>
							hard.base
						case (arg: SmolIr.TCall.TKArg) =>
							arg.kind
					}.map(Cpp.textForKind).reduce((_: String) + ", " + (_: String))
				else
					"void"

			val result: String =
				output match {
					case typeDef: SmolIr.TypeDef => Cpp.textForKind(typeDef.base)
					case _ => Cpp.textForKind(output)
				}

			s"$result(SMOL_CALL)($castArgs)"
		}

		val callArgs: String =
			if (fullArgs.nonEmpty)
				fullArgs.map {
					case SmolIr.TCall.Arg(param, kind) =>
						import Examine._
						kind match {
							case hard: SmolIr.TypeDef if hard.isHard =>
								s"reinterpret_cast<${Cpp.textForKind(hard.base.Const.Ref)}>(${param.text})"
							case _ =>
								param.text
						}

					case SmolIr.TCall.ThisArg => selfData.text
					case value: SmolIr.TCall.Value => value.hex.text
				}.reduce((_: String) + ", " + (_: String))
			else
				""

		s"(reinterpret_cast<$castKind>(_$moduleName._${name.text}))($callArgs)"
	}

	def emitCallable(callable: SmolIr.TCall, head: String, pre: String, call: String, end: String): String =
		(pre, callable.kind, end) match {
			case ("return ", enum: SmolIr.EnumKind, "") =>

				val tail: String =
					Cpp.emitAssertEnumOrFlag(
						moduleName,
						enum,
						"_out"
					)

				s"""
					 |$head
					 |{${emitArgChecks(callable.args)}
					 |	auto _out = $call;$tail
					 |	return _out;
					 |}
				""".stripMargin.trim + '\n'

			case _ =>
				s"""
					 |$head
					 |{${emitArgChecks(callable.args)}
					 |	$pre$call$end;
					 |}
				""".stripMargin.trim + '\n'
		}

	def emitArgChecks(args: SmolIr.TCall.Args): String =
		args.filterTo[SmolIr.TCall.Arg].filter {
			arg: SmolIr.TCall.Arg =>
				import Examine._
				(arg.kind.isInstanceOf[SmolIr.TypeDef] && arg.kind.asInstanceOf[SmolIr.TypeDef].isHard) || arg.kind.isInstanceOf[SmolIr.EnumKind]
		} match {
			case Nil => ""
			case enumArgs: List[SmolIr.TCall.Arg] =>
				enumArgs
					.map {
						case SmolIr.TCall.Arg(name: TS.Tok, hard: SmolIr.TypeDef) =>
							hard.hardCheck(
								s"reinterpret_cast<const ${Cpp.textForKind(hard.base)}&>(${name.text})"
							)

						case SmolIr.TCall.Arg(name: TS.Tok, enum: SmolIr.EnumKind) =>
							Cpp.emitAssertEnumOrFlag(
								moduleName,
								enum,
								name.text
							)
					}.reduce((_: String) + (_: String))
		}

	implicit class WrapForHardCheck(hard: SmolIr.TypeDef) {

		import Examine._

		assume(hard.isHard)

		def hardCheck(read: String): String =
			s"\n\tassert((${hard.value.text.drop(2).dropRight(2)}) != $read);"
	}

}

