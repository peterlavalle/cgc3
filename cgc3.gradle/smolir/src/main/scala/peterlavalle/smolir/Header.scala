package peterlavalle.smolir

import java.io.{StringWriter, Writer}

import peterlavalle.TS
import SmolIr.TCall

// TODO ; remove the implicit
class Header(m: SmolIr.Module) {
	implicit val module: SmolIr.Module = m

	import Examine._

	val examine: Examine = new Examine(module)

	val moduleName: String = module.name.text
	val prefixName: String = module.prefix.text

	def apply[W <: Writer](writer: W): W = {

		writer
			.appund('\n' +
				s"""
					 |#pragma once
					 |
					 |#include <stdint.h>
					 |
					 |struct ${module.name.text}
					 |{
				""".stripMargin.trim + '\n'
			)

			.appendSection("// enumeration types\n", examine.allEnums) {
				// expand enums
				case SmolIr.EnumKind(name, flex, base, items) =>

					val hard: String =
						if (SmolIr.Flex.Hard == flex)
							"class "
						else
							""

					new StringWriter()
						.appund(s"\tenum $hard${name.text}: ${Cpp.textForKind(base)}\n")
						.appund(s"\t{\n")
						.appund(items) {
							item: SmolIr.Enumerant =>
								s"\t\t${item.label.text} = ${item.value.text},\n"
						}
						.appund(s"\t};\n")
						.toString
			}

			.appendSection("// class types\n", examine.allClasses) {
				typeDef: SmolIr.TypeDef =>
					val SmolIr.TypeDef(name, base, value, members) = typeDef

					def initValue: String =
						if (null != value)
							" = (" + value.text.drop(2).dropRight(2) + ')'
						else
							""

					new StringWriter()
						.appund {
							if (typeDef.isHard) {
								s"""
									 |class ${name.text} final // hard
									 |{
									 |	${Cpp.textForKind(base)} _this;
									 |	${name.text}(${Cpp.textForKind(base)});
									 |public:
									 |	${name.text}(void);
									 |	${name.text}(const ${name.text}&) = delete;
									 |	${name.text}& operator=(const ${name.text}&) = delete;
									 |	${name.text}(${name.text}&&);
									 |	void operator=(${name.text}&&);
									 |
							 	""".stripMargin.trim.reIndent(1) + '\n' + '\n'
							} else {
								//TODO; de-inline
								s"""
									 |struct ${name.text} final
									 |{
									 |	${Cpp.textForKind(base)} _this;
									 |	inline ${name.text}(${Cpp.textForKind(base)} _$initValue) : _this(_) {}
									 |	inline operator ${Cpp.textForKind(base)}(void) const { return _this; }
									 |
							 	""".stripMargin.trim.reIndent(1) + '\n'
							}
						}
						.appund(members) {
							case SmolIr.TypeDef.Constructor(code, args) =>
								s"\t\tstatic ${name.text} ${code.text}(${Header.argsToString(args.filter(_.isInstanceOf[SmolIr.TCall.Arg]))});\n"

							case SmolIr.TypeDef.Destructor(code, args) =>
								require(args.filterTo[SmolIr.TCall.Arg].isEmpty)
								require(typeDef.isHard)
								s"\t\t~${name.text}(void); // ${code.text}\n"

							case SmolIr.TypeDef.Method(code, _, args, kind) =>
								s"\t\t${Cpp.textForKind(kind)} ${code.text}(${
									Header.argsToString(
										args.filterNot(SmolIr.TCall.ThisArg == _)
									)
								});\n"
						}
						.appund(s"\t};\n")
						.toString
			}

			.appendSection("// calls\n", examine.distinctCallableGroups) {
				case ("", data) =>
					new StringWriter()
						.appund(data) {
							case SmolIr.Prototype(code, name, args, kind) =>
								List(
									s"void* _${name.text};",
									s"static ${Cpp.textForKind(kind)} ${code.text}(${Header.argsToString(args)});"
								).foldLeft("")((_: String) + "\t" + (_: String) + "\n")
						}
						.toString
				case (name: String, data) =>
					new StringWriter()
						.appund(s"\t// $name\n")
						.appund(data) {
							case member: SmolIr.Member =>
								s"\t\tvoid* _${member.name.text};\n"
						}
						.toString
			}

			.appund(
				s"""
					 |// initialiser
					 |	static void def(void*, void*(*)(void*, const char*), const char*);
					 |};
				""".stripTrim
			)
			.appund("#if defined(smol_cpp)\n")

	}
}

object Header {
	def names(names: List[TS.Tok])(implicit module: SmolIr.Module): String =
		names match {
			case Nil => ""
			case _ =>
				names.map((_: TS.Tok).text).reduce((_: String) + "," + (_: String))
		}

	def argsToString(what: Iterable[SmolIr.TCall.TArg])(implicit module: SmolIr.Module): String =
		what match {
			case args: SmolIr.TCall.Args =>
				args.filterNot((_: TCall.TArg).isInstanceOf[SmolIr.TCall.Value]) match {
					case Nil => "void"
					case list =>
						list.filterNot((_: TCall.TArg).isInstanceOf[SmolIr.TCall.Value]).map {
							case (SmolIr.TCall.ThisArg) =>
								sys.error(
									"trying to convert a `this` to an arg; should have been handled higher"
								)
							case (arg: SmolIr.TCall.Arg) => Header.nameAndKind(arg)
						}.reduce((_: String) + ", " + (_: String))
				}

			case _ =>
				argsToString(what.toList)

		}

	def nameAndKind(arg: SmolIr.TCall.Arg)(implicit module: SmolIr.Module): String = {
		import Examine._
		arg match {
			case SmolIr.TCall.Arg(name, hard: SmolIr.TypeDef) if hard.isHard =>
				Cpp.textForKind(hard.Const.Ref) + ' ' + name.text
			case SmolIr.TCall.Arg(name, kind) =>
				Cpp.textForKind(kind) + ' ' + name.text
		}
	}
}
