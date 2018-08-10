package peterlavalle.smolir

import scala.io.Source

object HardCoded {
	lazy val loader: String = {
		val text: String =
			Source.fromInputStream(
				getClass
					.getResourceAsStream("smol_load.c")
			)
				.mkString

		text
			.replace("%{smol_load.c.hashCode()}", s"${text.hashCode}")
	}
}
