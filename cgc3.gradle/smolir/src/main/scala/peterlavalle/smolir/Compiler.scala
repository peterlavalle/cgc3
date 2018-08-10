package peterlavalle.smolir

import java.util

import org.antlr.v4.runtime.tree.TerminalNode
import peterlavalle.TS

import scala.util.matching.Regex

object Compiler {

	private val rPrimitiveReal: Regex = "real(32|64)".r

	private val rPrimitiveInt: Regex = "(u|s)int(8|16|32|64)".r

	def apply(module: SmolIrParser.ModuleContext): SmolIr.Module =
		(module.Name().map(TS.apply).toList match {
			case List(name: TS.Tok, prefix: TS.Tok) =>
				(name, prefix)
			case List(name: TS.Tok) =>
				(name, TS(""))
		}) match {
			case (name: TS.Tok, prefix: TS.Tok) =>

				val t: Context.T = module.content().toList.foldLeft(Context.fresh) {
					case (context: Context.T, entry: SmolIrParser.ContentContext) =>

						val content: SmolIr.TContent =
							topLevelContext(context, entry)

						context.bind(content.name, content)
				}

				SmolIr.Module(
					name, prefix,
					t.binding.reverse
				)
		}

	println("Upstream this")

	implicit class TokExt(tok: TS.Tok) {
		def rename(lambda: String => String): TS.Tok =
			TS.Tok(
				lambda(tok.text)
			)(
				tok.file,
				tok.line
			)
	}

	def compileArgs(context: Context.T, argsContexts: util.List[SmolIrParser.ArgsContext]): SmolIr.TCall.Args =
		argsContexts.toList.zipWithIndex.map {
			case (argsContext: SmolIrParser.ArgsContext, index: Int) =>
				argsContext match {
					case normalContext: SmolIrParser.NormalContext =>
						val tok: TS.Tok = normalContext.Name().ifNullOrMap(TS(s"arg$index"))(TS.apply)
						(
							tok,
							Compiler(context.nest(tok.text), normalContext.kin())
						)
					case _: SmolIrParser.SelfieContext =>
						SmolIr.TCall.ThisArg

					case hardcoded: SmolIrParser.HardcodedContext =>
						SmolIr.TCall.Value(
							hardcoded.HexVal(),
							compileAtomic(
								hardcoded.AtomicWhole()
							)
						)
				}
		}
			// expand any compound parameters that need to be multiple ones in real life
			.flatMap {
			case SmolIr.TCall.ThisArg =>
				List(SmolIr.TCall.ThisArg)

			case value: SmolIr.TCall.Value =>
				List(value)

			case (tok: TS.Tok, SmolIr.KindVector(dimen: SmolIr.TNaturalKind, List(one: SmolIr.TKind))) =>
				List(
					SmolIr.TCall.Arg(
						tok.rename((_: String) + "_size"),
						dimen
					),

					SmolIr.TCall.Arg(
						tok.rename((_: String) + "_head"),
						SmolIr.KindPointer(
							one
						)
					)
				)

			case (tok: TS.Tok, SmolIr.KindVector(dimen: SmolIr.TNaturalKind, many: List[SmolIr.TKind])) =>
				SmolIr.TCall.Arg(
					tok.rename((_: String) + "_size"),
					dimen
				) :: many.zipWithIndex.map {
					case (next: SmolIr.TKind, idx: Int) =>
						SmolIr.TCall.Arg(
							tok.rename((_: String) + s"_head_$idx"),
							SmolIr.KindPointer(
								next
							)
						)
				}

			case (tok: TS.Tok, kin: SmolIr.TKind) =>
				List(
					SmolIr.TCall.Arg(
						tok,
						kin
					)
				)
		}

	def topLevelContext(context: Context.T, content: SmolIrParser.ContentContext): SmolIr.TContent =
		content match {

			case enum: SmolIrParser.Enumerant_contentContext =>


				SmolIr.EnumKind(
					enum.Name(),
					enum.EnumFlag(),
					compileAtomic(enum.AtomicWhole()),
					enum.`with`().flatMap {
						from: SmolIrParser.WithContext =>
							context.find(from.Name()) match {
								case SmolIr.EnumKind(_, _, _, list) =>
									list
							}
					}.toList ++ compileEnumerants(
						enum.enumerant()
					)
				)

			case aliasContext: SmolIrParser.AliasContext =>
				val typeName: TS.Tok = aliasContext.Name()

				def compile(member: SmolIrParser.MemberContext): SmolIr.TypeDef.TMember =
					member match {
						case constructor: SmolIrParser.ConstructorContext =>
							val node: TS.Tok = constructor.Name()
							SmolIr.TypeDef.Constructor(
								node,
								compileArgs(
									context.nest(node.text), constructor.args()
								).map {
									case arg: SmolIr.TCall.Arg => arg
									case arg: SmolIr.TCall.Value => arg
									case SmolIr.TCall.ThisArg => SmolIr.TCall.ThisArg
								}
							)
						case destructor: SmolIrParser.DestructorContext =>
							val node: TS.Tok = destructor.Name()
							SmolIr.TypeDef.Destructor(
								node,
								compileArgs(
									context.nest(node.text), destructor.args()
								).map {
									case arg: SmolIr.TCall.Arg =>
										sys.error(
											"args don't work here"
										)
									case arg: SmolIr.TCall.Value => arg
									case SmolIr.TCall.ThisArg => SmolIr.TCall.ThisArg
								}
							)

						case method: SmolIrParser.MethodContext =>
							val (mCode: TS.Tok, mName: TS.Tok) =
								pairForCodeContext(
									method.code()
								)

							val explicitArgs: SmolIr.TCall.Args =
								compileArgs(
									context
										.nest(typeName)
										.nest(mCode),
									method.prototype().args()
								)

							SmolIr.TypeDef.Method(
								mCode, mName,
								if (explicitArgs.contains(SmolIr.TCall.ThisArg))
									explicitArgs
								else
									SmolIr.TCall.ThisArg :: explicitArgs,
								Compiler(
									context
										.nest(typeName)
										.nest(mCode),
									method.prototype().kin()
								)
							)
					}

				val typeDef =

					SmolIr.TypeDef(
						typeName,
						compileKind(context.nest(typeName), aliasContext.kin().kind()),
						if (null != aliasContext.HereSource())
							aliasContext.HereSource()
						else
							null,
						aliasContext.member().toList.map(compile)
					)
				import Examine._

				aliasContext.whinge().getText match {
					case "auto" | "type" =>
						typeDef

					case "hard" =>
						require(
							typeDef.isHard,
							s"I needed ${typeName.text} to be hard"
						)
						typeDef

					case "soft" =>
						require(
							!(typeDef.isHard),
							s"I needed ${typeName.text} to be soft"
						)
						typeDef

					case "firm" =>
						sys.error(
							"firm types (hard but unchecked) aren't yet supported"
						)
				}


			case prototype: SmolIrParser.Prototype_contentContext =>
				val (mCode: TS.Tok, mName: TS.Tok) =
					pairForCodeContext(
						prototype.code()
					)


				SmolIr.Prototype(
					mCode, mName,
					compileArgs(context.nest(mCode), prototype.prototype().args()).map { case arg: SmolIr.TCall.Arg => arg },
					Compiler(
						context.nest(mCode.text),
						prototype.prototype().kin()
					)
				)
		}

	def apply(context: Context.T, kinContext: SmolIrParser.KinContext): SmolIr.TKind =
		kinContext match {
			case null =>
				SmolIr.KVoid
			case _ =>
				compileKind(
					context,
					kinContext.kind()
				)
		}

	def compileKind(context: Context.T, kindContext: SmolIrParser.KindContext): SmolIr.TKind =
		kindContext match {
			case constant: SmolIrParser.ConstantContext =>
				compileKind(context, constant.kind()).Const

			case referenceContext: SmolIrParser.ReferenceContext =>
				compileKind(context, referenceContext.kind()).Ref

			case vectorArray: SmolIrParser.VectorContext =>
				SmolIr.KindVector(
					compileAtomic(vectorArray.AtomicWhole()).asInstanceOf[SmolIr.TNaturalKind],
					vectorArray.kind().toList.zipWithIndex.map {
						case (v, i: Int) =>
							compileKind(context.nest(i.toString), v)
					}
				)

			case inplaceEnum: SmolIrParser.Inplace_enumContext =>
				SmolIr.EnumKind(

					// name
					TS.Tok(context.nesting.foldLeft(inplaceEnum.EnumFlag().getText.trim.substring(0, 1)) {
						case (l, r) =>
							r + "_" + l
					})(
						inplaceEnum.getStart.getTokenSource.getSourceName,
						inplaceEnum.getStart.getLine
					),

					// flexiness
					inplaceEnum.EnumFlag(),

					// base
					compileAtomic(inplaceEnum.AtomicWhole()),

					// enumerants
					compileEnumerants(inplaceEnum.enumerant())
				)

			case named: SmolIrParser.NamedContext =>
				context.find(named.Name()) match {
					case enum: SmolIr.EnumKind => enum
					case typeDef: SmolIr.TypeDef => typeDef
				}

			case pointerContext: SmolIrParser.PointerContext =>
				compileKind(context, pointerContext.kind()).Ptr

			case primitiveContext: SmolIrParser.PrimitiveContext =>
				primitiveContext.children.toList match {
					case List(token: TerminalNode) =>
						compileAtomic(token)
				}
		}

	def compileEnumerants(enumerantContexts: util.List[SmolIrParser.EnumerantContext]): List[SmolIr.Enumerant] =
		enumerantContexts.toList.map {
			enumerantContext: SmolIrParser.EnumerantContext =>
				SmolIr.Enumerant(
					enumerantContext.Name(),
					enumerantContext.HexVal()
				)
		}

	def compileAtomic(terminalNode: TerminalNode): SmolIr.TAtomicKind =
		terminalNode.getText match {
			case "void" => SmolIr.KVoid
			case "char" => SmolIr.KindChar
			case "size_t" => SmolIr.KindSize
			case rPrimitiveReal(size: String) =>
				size match {
					case "32" => SmolIr.KindReal32
					case "64" => SmolIr.KindReal64
				}
			case rPrimitiveInt(sign: String, size: String) =>
				sign + size match {
					case "s8" => SmolIr.KindIntS8
					case "s16" => SmolIr.KindIntS16
					case "s32" => SmolIr.KIntS32
					case "s64" => SmolIr.KindIntS64
					case "u8" => SmolIr.KIntU8
					case "u16" => SmolIr.KIntU16
					case "u32" => SmolIr.KindIntU32
					case "u64" => SmolIr.KindIntU64
				}
		}


	object Context {

		def fresh: T = Fresh

		trait T {
			def nesting: List[String]

			def binding: List[SmolIr.TContent]

			def nest(mine: TS.Tok): Context.T = Nest(this, mine)

			def find(name: TS.Tok): SmolIr.TContent

			def bind(link: TS.Tok, data: SmolIr.TContent): Context.T = Bind(this, link, data)
		}

		case class Nest private(root: T, link: TS.Tok) extends T {
			override def nesting: List[String] = link.text :: root.nesting

			override def binding: List[SmolIr.TContent] = root.binding

			override def find(name: TS.Tok): SmolIr.TContent = root find name
		}

		case class Bind private(root: T, link: TS.Tok, data: SmolIr.TContent) extends T {

			override def binding: List[SmolIr.TContent] = data :: root.binding

			override def nesting: List[String] = root.nesting

			override def find(name: TS.Tok): SmolIr.TContent =
				if (name.text != link.text)
					root find name
				else
					data
		}

		case object Fresh extends T {
			override def nesting: List[String] = Nil

			override def binding: List[SmolIr.TContent] = Nil

			override def find(name: TS.Tok): SmolIr.TContent =
				throw new Exception(
					List(
						s"No type known for name `${name.text}`",
						s"@${name.file}",
						s":${name.line}"
					).reduce((_: String) + "\n\t" + (_: String))
				)
		}

	}

	import scala.language.implicitConversions

	implicit def enumFlag(node: TerminalNode): SmolIr.Flex.T =
		node.getText match {
			case "enum" => SmolIr.Flex.Hard
			case "flag" => SmolIr.Flex.Soft
		}

	implicit def pairForCodeContext(codeContext: SmolIrParser.CodeContext): (TS.Tok, TS.Tok) =
		codeContext match {
			case direct: SmolIrParser.DirectContext =>
				val name: TS.Tok = direct.Name()
				(name, name)
			case rename: SmolIrParser.RenameContext =>
				val List(c, n) = rename.Name().toList
				val code: TS.Tok = c
				val name: TS.Tok = n
				(code, name)
		}
}
