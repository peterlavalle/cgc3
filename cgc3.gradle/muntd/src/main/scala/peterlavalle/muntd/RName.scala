package peterlavalle.muntd

object RName {
	def apply(l: Int): String =
		RName(
			('a' to 'z').toList ++ ('A' to 'Z'),
			l
		)

	def apply(letters: List[Char], l: Int): String =
		new String(
			Stream.continually {
				letters((Math.random() * letters.size).toInt)
			}.take(l).toArray
		)
}
