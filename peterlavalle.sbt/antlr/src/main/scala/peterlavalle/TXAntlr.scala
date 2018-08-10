package peterlavalle


trait TXAntlr {

	import org.antlr.v4.runtime._

	implicit class WrappedCharStream[S <: ANTLRInputStream](antlrInputStream: S) {
		def setName(name: String): S = {
			antlrInputStream.name = name
			antlrInputStream
		}
	}

	implicit def wrapCommonTokenStream(tokenSource: TokenSource): CommonTokenStream =
		new CommonTokenStream(tokenSource)

	implicit class WrappedRecognizer[R <: Recognizer[_, _]](recognizer: R) {
		def handleErrors(handler: (RecognitionException, String, Int) => Unit): R = {
			recognizer.removeErrorListeners()
			recognizer.addErrorListener(
				new ConsoleErrorListener {

					// ... IntelliJ's error message here drives me up the wall
					override def syntaxError(recognizer: Recognizer[_, _], offendingSymbol: scala.Any, line: Int, charPositionInLine: Int, msg: String, e: RecognitionException): Unit =
						handler(e, msg, line)
				}
			)
			recognizer
		}
	}

}
