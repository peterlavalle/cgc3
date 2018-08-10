package peterlavalle

import scala.io.Source

trait TTestCase {

	this: ATestCase =>

	def nodeTree: CNode.TNode

	def testParseIt(): Unit = {
		sAssertEqual(
			nodeTree,
			CNode.parse(nodeSource)
		)
	}

	lazy val nodeSource: Stream[Char] =
		Source.fromInputStream(
			getClass.getResourceAsStream(s"${getClass.getSimpleName}.n")
		).toList.toStream

}
