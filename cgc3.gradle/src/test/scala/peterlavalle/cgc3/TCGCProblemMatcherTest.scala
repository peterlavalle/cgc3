package peterlavalle.cgc3

import java.util.regex.Matcher

import org.junit.Assert._
import peterlavalle.ATestCase

class TCGCProblemMatcherTest extends ATestCase {

	/**
		* test an actual error that I encountered
		*/
	def testCoffeeTinError(): Unit =
		new Match {
			override def file: String =
				"C:/Users/Peter/Desktop/portfolio/portfolio-cgc3/cgc3.gradle/examples/coffeescript/coffeescript-test-shared/build/Windows_7/amd64/tin/src/tin.h"

			override def line: Int = 62

			override def column: Int = 1

			override def severity: String = "error"

			override def message: String = "expected '}' at end of input"
		} matches {
			"(:coffeescript-test-shared:compile! tin.c!C:/Users/Peter/Desktop/portfolio/portfolio-cgc3/cgc3.gradle/examples/coffeescript/coffeescript-test-shared/build/Windows_7/amd64/tin/src/tin.h:62:1: error: expected '}' at end of input,)"
		}

	/**
		* test a fake error that I added to verify that this worked
		*/
	def testFakeError(): Unit =
		new Match {
			override def file: String =
				"C:\\Users\\Peter\\Desktop\\portfolio\\portfolio-cgc3\\cgc3.gradle\\examples\\basic\\shared\\src\\shared.c"

			override def line: Int = 6

			override def column: Int = 5

			override def severity: String = "error"

			override def message: String = "'hey' was not declared in this scope"
		} matches {
			"(:shared:compile! shared.c!C:\\Users\\Peter\\Desktop\\portfolio\\portfolio-cgc3\\cgc3.gradle\\examples\\basic\\shared\\src\\shared.c:6:5: error: 'hey' was not declared in this scope,)"
		}

	trait Match {
		def file: String

		def line: Int

		def column: Int

		def severity: String

		def message: String

		def matches(output: String): Unit = {

			val matcher: Matcher = TCGCProblemMatcher.rPattern.pattern.matcher(output)

			// the statement should match
			assertTrue(matcher.find())

			// see if we got the expected filename
			assertEquals(file, matcher.group(TCGCProblemMatcher.gPatternFile))

			// see if we got the expected line
			assertEquals(line, matcher.group(TCGCProblemMatcher.gPatternLine).toInt)

			// see if we got the expected column
			assertEquals(column, matcher.group(TCGCProblemMatcher.gPatternColumn).toInt)

			// see if we got the expected message
			assertEquals(message, matcher.group(TCGCProblemMatcher.gPatternMessage))

			// see if we got the expected severity
			assertEquals(severity, matcher.group(TCGCProblemMatcher.gPatternSeverity))
		}
	}

}
