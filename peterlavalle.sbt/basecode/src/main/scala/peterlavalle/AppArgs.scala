package peterlavalle

import java.io.File

trait AppArgs {

	this: App =>

	def arg(name: String, default: String): String = {
		val key = s"-$name="
		(args.toList.filter((_: String).startsWith(key)) match {
			case Nil =>
				System.getProperty(name, default)
			case List(value) =>
				value.substring(key.length)
		}) match {
			case value: String if value.startsWith("'") && value.endsWith("'") => ???
			case value: String if value.startsWith("\"") && value.endsWith("\"") => ???
			case value: String =>
				value
		}
	}

	def arg(name: String, default: File): File =
		new File(arg(name, default.AbsolutePath)).getAbsoluteFile

	def argRoot(name: String, from: File => File = (f: File) => f): File =
		arg(name, from(new File("from").ParentFile))
}
