package peterlavalle.smolir

import peterlavalle.TS
import SmolIr.TypeDef


class Examine(module: SmolIr.Module) {

	def allClasses: Iterable[SmolIr.TypeDef] = {
		module.contents.toStream.flatMap {
			case kind: SmolIr.TypeDef =>
				List(kind)
			case _: SmolIr.Prototype |
					 _: SmolIr.EnumKind =>
				// the present system doesn't allow inlaid classes
				Nil
		}
	}

	def allCallableGroups: Iterable[(String, Iterable[SmolIr.TCall])] =
		allCallable.clusterBy {
			case _: SmolIr.Prototype => ""
			case member: SmolIr.Member => member.self.name.text
		}

	def allCallable: Iterable[SmolIr.TCall] = {
		module.contents.toStream.flatMap {
			case prototype: SmolIr.Prototype =>
				List(prototype)

			case typeDef: SmolIr.TypeDef =>
				typeDef.members.map(SmolIr.Member(typeDef, _: TypeDef.TMember))

			case _: SmolIr.EnumKind =>
				// the present system doesn't allow inlaid classes
				Nil
		}
	}

	def distinctCallableGroups: Iterable[(String, Iterable[SmolIr.TCall])] =
		allCallable.distinctBy(_.name.text).clusterBy {
			case _: SmolIr.Prototype => ""
			case member: SmolIr.Member => member.self.name.text
		}

	def allEnums: Iterable[SmolIr.EnumKind] = {
		module.contents.toStream.flatMap {
			case enum: SmolIr.EnumKind =>
				List(enum)
			case SmolIr.Prototype(code: TS.Tok, _: TS.Tok, args: List[SmolIr.TCall.Arg], kind: SmolIr.TKind) =>
				(kind :: args.map((_: SmolIr.TCall.Arg).kind).reverse).reverse.filterTo[SmolIr.EnumKind]
			case typeDef: SmolIr.TypeDef =>

				typeDef.members.flatMap {
					case SmolIr.TypeDef.Constructor(_, args) =>
						args.map {
							case arg: SmolIr.TCall.TKArg =>
								arg.kind
							case SmolIr.TCall.ThisArg => SmolIr.TCall.ThisArg
						}

					case _: SmolIr.TypeDef.Destructor =>
						// destructors can't have args
						Nil

					case SmolIr.TypeDef.Method(_, _: TS.Tok, args: SmolIr.TCall.Args, kind: SmolIr.TKind) =>
						args.map {
							case SmolIr.TCall.ThisArg => SmolIr.TCall.ThisArg
							case (arg: SmolIr.TCall.Arg) => arg.kind
							case (value: SmolIr.TCall.Value) => value.kind
						} ++ List(kind)
				}.filterTo[SmolIr.EnumKind]
		}.distinct
	}
}

object Examine {

	implicit class TExamineTypeDef(typeDef: TypeDef) {
		def isHard: Boolean = {
			typeDef.members.filterTo[SmolIr.TypeDef.Destructor].nonEmpty
		}

		require(null != typeDef.value || !isHard)
	}

}
