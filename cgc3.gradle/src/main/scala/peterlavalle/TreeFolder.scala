package peterlavalle

import java.io.File
import java.util.regex.Pattern

case class TreeFolder(root: File, pattern: Pattern) extends Iterable[(TreeFolder, String)] {

	def lastModified: Option[Long] =
		sys.error("calculte dumb last-modified")

	override def iterator: Iterator[(TreeFolder, String)] =
		(root ** pattern.pattern()).map(this -> (_: String)).iterator
}

object TreeFolder {
	implicit def TreeFolder(root: File): TreeFolder =
		TreeFolder(root, ".+")

	implicit def TreeFolder(root: File, regex: String): TreeFolder =
		new TreeFolder(root, Pattern.compile(regex))

	implicit def TreeFolder(pair: (File, String)): TreeFolder =
		pair match {
			case (root: File, regex: String) =>
				new TreeFolder(root, Pattern.compile(regex))
		}
}
