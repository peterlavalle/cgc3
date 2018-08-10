package peterlavalle.cgc3

import org.json.JSONObject

import scala.util.matching.Regex

trait TCGCProblemMatcher {

	this: VCodeGradle.TVCTaskLeaf =>

	def problemMatcher: Option[Object] =
		Some {
			// https://code.visualstudio.com/docs/editor/tasks#_defining-a-problem-matcher
			new JSONObject()
				// The problem is owned by the cpp language service.
				.put("owner", "cpp")
				// The file name for reported problems is relative to the opened folder.
				.put("fileLocation", "absolute")
				// The actual pattern to match problems in the output.
				.put("pattern",
				new JSONObject()
					// The regular expression. Example to match: helloWorld.c:5:3: warning: implicit declaration of function ‘printf’ [-Wimplicit-function-declaration]
					.put("regexp", TCGCProblemMatcher.rPattern.regex)
					// The first match group matches the file name which is relative.
					.put("file", TCGCProblemMatcher.gPatternFile)
					// The second match group matches the line on which the problem occurred.
					.put("line", TCGCProblemMatcher.gPatternLine)
					// The third match group matches the column at which the problem occurred.
					.put("column", TCGCProblemMatcher.gPatternColumn)
					// The fourth match group matches the problem's severity. Can be ignored. Then all problems are captured as errors.
					.put("severity", TCGCProblemMatcher.gPatternSeverity)
					// The fifth match group matches the message.
					.put("message", TCGCProblemMatcher.gPatternMessage)
			)
		}
}

object TCGCProblemMatcher {
	val rPattern: Regex = "^.[^!]+![^!]+!(...[^:]+):(\\d+):(\\d+): ([^:]+): (.+),\\)$".r
	val gPatternFile: Int = 1
	val gPatternLine: Int = 2
	val gPatternColumn: Int = 3
	val gPatternSeverity: Int = 4
	val gPatternMessage: Int = 5
}
