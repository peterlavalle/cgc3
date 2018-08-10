package peterlavalle.smolir

import peterlavalle.TS

object SmolIr {

	def Prototype(name: TS.Tok, args: List[TCall.Arg], kind: TKind): Prototype =
		new Prototype(name, name, args, kind)

	sealed trait TContent {
		val name: TS.Tok
	}

	sealed trait TKind {
		def name: TS.Tok

		def Ptr: KindPointer = KindPointer(this)

		def Ref: KindReference = KindReference(this)

		def Const: KindConstant = KindConstant(this)
	}

	sealed trait TAtomicKind extends TKind {
		val name: TS.Tok = getClass.getSimpleName.toLowerCase().substring(4)
	}

	sealed trait TCall {
		val name: TS.Tok
		val args: TCall.Args
		val kind: TKind
	}

	sealed trait TWholeKind extends TAtomicKind

	sealed trait TNaturalKind extends TWholeKind

	case class Module
	(
		name: TS.Tok,
		prefix: TS.Tok,
		contents: List[TContent]
	)

	case class Prototype
	(
		code: TS.Tok,
		name: TS.Tok,
		args: List[TCall.Arg],
		kind: TKind
	) extends TContent with TCall

	/**
		* Wraps a member to make it play like a top-level function
		*/
	case class Member
	(
		self: TypeDef,
		method: TypeDef.TMember
	) extends TCall {
		override val name: TS.Tok = method.name
		override val args: TCall.Args = TCall.ThisArg :: method.args
		override val kind: TKind =
			method match {
				case _: TypeDef.Constructor => self
				case _: TypeDef.Destructor => KVoid
				case TypeDef.Method(_, _, _, k) => k
			}
	}

	case class TypeDef
	(
		name: TS.Tok,
		base: TKind,
		value: TS.Tok,
		members: List[TypeDef.TMember]
	) extends TContent with TKind

	case class EnumKind
	(
		name: TS.Tok,
		flex: Flex.T,
		base: TKind,
		enumerants: List[Enumerant]
	) extends TContent with TKind

	case class Enumerant
	(
		label: TS.Tok,
		value: TS.Tok
	)

	case class KindVector
	(
		dimension: TWholeKind,
		contents: List[TKind]
	) extends TKind {
		override def name: TS.Tok = ???
	}

	case class KindPointer
	(
		kind: TKind
	) extends TKind {
		val name: TS.Tok =
			TS.Tok(s"*${kind.name.text}")(
				kind.name.file,
				kind.name.line
			)
	}

	case class KindReference
	(
		kind: TKind
	) extends TKind {
		val name: TS.Tok =
			TS.Tok(
				s"&${kind.name.text}"
			)(
				kind.name.file,
				kind.name.line
			)
	}

	case class KindConstant
	(
		kind: TKind
	) extends TKind {
		val name: TS.Tok =
			TS.Tok(
				s"#${kind.name.text}"
			)(
				kind.name.file,
				kind.name.line
			)
	}

	case class KindSelf
	(
		name: TS.Tok
	) extends TKind

	/**
		* used for "hard" or "soft" types
		*/
	object Flex {

		sealed trait T

		case object Hard extends T

		case object Soft extends T

	}

	object TCall {

		type Args = List[TArg]

		trait TArg

		trait TKArg extends TArg {
			val kind: TKind
		}

		case class Value(hex: TS.Tok, kind: TKind) extends TKArg

		case class Arg(name: TS.Tok, kind: TKind) extends TKArg

		case object ThisArg extends TArg with TKind {
			override def name: TS.Tok = sys.error("don't do this")
		}

	}

	object TypeDef {

		def Method(name: TS.Tok, args: TCall.Args, kind: TKind): Method =
			Method(name, name, args, kind)

		sealed trait TMember {
			val name: TS.Tok
			val args: TCall.Args
		}

		case class Constructor
		(
			name: TS.Tok,
			args: TCall.Args
		) extends TMember

		case class Destructor
		(
			name: TS.Tok,
			args: TCall.Args = Nil
		) extends TMember {
			args.foreach {
				arg: TCall.TArg =>
					require(!arg.isInstanceOf[TCall.Arg])
			}
		}

		case class Method
		(
			code: TS.Tok,
			name: TS.Tok,
			args: TCall.Args,
			kind: TKind
		) extends TMember with TCall

	}

	case object KVoid extends TAtomicKind

	case object KindChar extends TAtomicKind

	case object KindSize extends TNaturalKind

	case object KIntU8 extends TNaturalKind

	case object KindIntS8 extends TWholeKind

	case object KIntU16 extends TNaturalKind

	case object KindIntS16 extends TWholeKind

	case object KindIntU32 extends TNaturalKind

	case object KIntS32 extends TWholeKind

	case object KindIntU64 extends TNaturalKind

	case object KindIntS64 extends TWholeKind

	case object KindReal64 extends TAtomicKind

	case object KindReal32 extends TAtomicKind

}
