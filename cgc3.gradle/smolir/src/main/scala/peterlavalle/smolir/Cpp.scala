package peterlavalle.smolir

import java.io.StringWriter

import peterlavalle.TS

object Cpp {
	def textForKind(kind: SmolIr.TKind)(implicit module: SmolIr.Module): String =
		kind match {

			case typeDef: SmolIr.TypeDef =>
				s"${module.name.text}::${typeDef.name.text}"

			case SmolIr.KindSelf(tok: TS.Tok) => s"${module.name.text}::${tok.text}"
			case SmolIr.KindSize => "size_t"
			case SmolIr.KVoid => "void"
			case SmolIr.KindChar => "char"
			case SmolIr.KindIntU64 => "uint64_t"
			case SmolIr.KindIntS64 => "int64_t"
			case SmolIr.KindIntU32 => "uint32_t"
			case SmolIr.KIntS32 => "int32_t"
			case SmolIr.KIntU16 => "uint16_t"
			case SmolIr.KindIntS16 => "int16_t"
			case SmolIr.KIntU8 => "uint8_t"
			case SmolIr.KindIntS8 => "int8_t"
			case SmolIr.KindReal64 => "double"
			case SmolIr.KindReal32 => "float"
			case SmolIr.KindConstant(element: SmolIr.TKind) => s"const ${textForKind(element)}"
			case SmolIr.KindPointer(element: SmolIr.TKind) => s"${textForKind(element)}*"
			case SmolIr.KindReference(element: SmolIr.TKind) => s"${textForKind(element)}&"

			case SmolIr.EnumKind(name, _, _, _) =>
				s"${module.name.text}::${name.text}"
		}


	def emitAssertEnumOrFlag(module: String, enum: SmolIr.EnumKind, name: String): String = {
		enum.flex match {
			case SmolIr.Flex.Hard =>
				new StringWriter()
					.appund("\n\tassert(")
					.appund(enum.enumerants.map {
						value: SmolIr.Enumerant =>
							s"\n\t\t($module::${enum.name.text}::${value.label.text} == $name)"
					}.reduce((_: String) + " ||" + (_: String)))
					.appund("\n\t);")
					.toString

			case SmolIr.Flex.Soft =>
				val values: String =
					enum.enumerants.map {
						value: SmolIr.Enumerant =>
							s"\n\t\t\t$module::${enum.name.text}::${value.label.text}"
					}.reduce((_: String) + " |" + (_: String))
				new StringWriter()
					.appund("\n\tassert(0 == (\n\t\t((")
					.appund(values)
					.appund(s"\n\t\t) | $name) ^ (")
					.appund(values)
					.appund("\n\t\t))\n\t);")
					.toString
		}
	}
}
