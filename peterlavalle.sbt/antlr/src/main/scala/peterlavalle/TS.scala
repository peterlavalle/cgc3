package peterlavalle

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

object TS {

	case class Tok
	(
		text: String
	)(
		val file: String,
		val line: Int
	)

	implicit def apply(value: String): Tok = {
		val frame: StackTraceElement = Thread.currentThread().getStackTrace.tail.tail.head
		Tok(
			value
		)(
			frame.getFileName,
			frame.getLineNumber
		)
	}

	implicit def apply(terminalNode: TerminalNode): Tok =
		Tok(
			terminalNode.getText
		)(
			terminalNode.getSymbol.getTokenSource.getSourceName,
			terminalNode.getSymbol.getLine
		)

	implicit def apply(parserRuleContext: ParserRuleContext): Tok =
		Tok(
			parserRuleContext.getText
		)(
			parserRuleContext.start.getTokenSource.getSourceName,
			parserRuleContext.start.getLine
		)
}
