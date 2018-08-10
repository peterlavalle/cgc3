package peterlavalle.smolir

import java.io.OutputStream

class Labels[O <: OutputStream](outputStream: O) {

	def apply(module: SmolIr.Module): O = {
		strings(module).foreach {
			name: String =>
				outputStream.write(
					name.getBytes
				)
				outputStream.write(
					Array[Byte](0)
				)
		}

		outputStream
	}

	def strings(module: SmolIr.Module): Iterable[String] = {
		val examine: Examine = new Examine(module)
		examine.allCallable.map {
			case SmolIr.Prototype(_, name, _, _) =>
				name
			case (member: SmolIr.Member) =>
				member.name
		}.map(_.text).distinctBy(_.toString)
	}

}
