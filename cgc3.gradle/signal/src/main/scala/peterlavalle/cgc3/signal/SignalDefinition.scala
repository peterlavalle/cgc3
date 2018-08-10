package peterlavalle.cgc3.signal

import java.io.InputStream

import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream, RecognitionException}
import peterlavalle.Possible
import peterlavalle.cgc3.{SignalHeaderLexer, SignalHeaderParser}

case class SignalDefinition
(
	name: peterlavalle.TS.Tok,

	inputs: Set[SignalDefinition.Input],
	outputs: Set[SignalDefinition.Output],

	events: Set[SignalDefinition.Event]
) {
	// SAN check to ensure that ordering is stable
	require(inputs.stringSorted.toList == inputs.toList)
	require(outputs.stringSorted.toList == outputs.toList)
	require(events.stringSorted.toList == events.toList)

	name.text match {
		case name: String =>
			require(
				name.matches("\\w+") || name.matches("\\w+\\s*:\\s*\\w+")
			)
	}
}

object SignalDefinition extends peterlavalle.TXAntlr {

	def parse(string: String): Possible[SignalDefinition] =
		parse(
			new ANTLRInputStream(string)
		)

	def parse(name: String, inputStream: InputStream): Possible[SignalDefinition] = {
		val stream = new ANTLRInputStream(inputStream)
		stream.name = name
		parse(
			stream
		)
	}

	def parse(antlrStream: org.antlr.v4.runtime.CharStream): Possible[SignalDefinition] = {

		case class Failure(fail: Possible[SignalDefinition]) extends Exception

		try {


			val definitionContext: SignalHeaderParser.DefinitionContext =
				new SignalHeaderParser(
					new CommonTokenStream(
						new SignalHeaderLexer(antlrStream)
							.handleErrors {
								case (_: RecognitionException, message: String, line: Int) =>
									throw Failure(
										Possible.fail(
											s"during lexical analysis `$message` at $line"
										)
									)
							}
					)
				).handleErrors {
					case (_: RecognitionException, message: String, line: Int) =>
						throw Failure(
							Possible.fail(
								s"during parsing `$message` at $line"
							)
						)
				}.definition()

			import peterlavalle.TS._
			import peterlavalle._

			lazy val memberSource: Iterable[AnyRef] =
				definitionContext.member().map {
					case input: SignalHeaderParser.InputContext =>
						Input(input.NAME(), input.kind())
					case output: SignalHeaderParser.OutputContext =>
						Output(output.NAME(), output.kind())
					case event: SignalHeaderParser.EventContext =>
						Event(event.NAME())
				}

			Possible.okay {
				SignalDefinition(
					definitionContext.kindname(),
					memberSource.filterTo[Input].toSet,
					memberSource.filterTo[Output].toSet,
					memberSource.filterTo[Event].toSet
				)
			}
		} catch {
			case failure: Failure =>
				failure.fail
		}
	}

	implicit class pSet2[T](set: Set[T]) {
		def stringSorted: Set[T] = stringSorted((_: T).toString)

		def stringSorted(to: T => String): Set[T] =
			set.toList.sortBy(to).toSet
	}

	case class Event(name: peterlavalle.TS.Tok)

	case class Input(name: peterlavalle.TS.Tok, kind: peterlavalle.TS.Tok)

	case class Output(name: peterlavalle.TS.Tok, kind: peterlavalle.TS.Tok)

}
