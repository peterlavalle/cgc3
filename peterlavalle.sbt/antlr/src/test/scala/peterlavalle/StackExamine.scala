package peterlavalle

object StackExamine extends App {


	def apply(text: String): String = {
		Thread.currentThread().getStackTrace.zipWithIndex.tail.tail.take(1).map {
			case (frame, index) =>
				s"\n\t[$index] = ${frame.getFileName}:${frame.getLineNumber}"
		}.foldLeft(s"text = `$text`")(_ + _)


	}


	println(
		apply("foobat")
	)

}

trait TThing {

}
