package peterlavalle.smolir

import java.io.{ByteArrayOutputStream, StringReader, StringWriter}

import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream, RecognitionException}
import org.junit.Assert._

trait TTestCase extends peterlavalle.ATestCase {
	def smol: String

	def treeCode: SmolIr.Module

	def header: String

	def expandedEnums: List[SmolIr.EnumKind]

	def expandedCalls: List[SmolIr.TCall]

	def testAllEnums(): Unit = {
		val actual: List[SmolIr.EnumKind] = new Examine(treeCode).allEnums.toList
		val expected: List[SmolIr.EnumKind] = expandedEnums
		sAssertEqual(
			"contents are different",
			expected.sortBy(_.name.text),
			actual.sortBy(_.name.text)
		)

		sAssertEqual(
			"ordering is different",
			expected,
			actual
		)
	}

	def testAllCallable(): Unit = {
		val actual: List[SmolIr.TCall] = new Examine(treeCode).allCallable.toList.sortBy((_: SmolIr.TCall).name.text)
		val expected: List[SmolIr.TCall] = expandedCalls.sortBy((_: SmolIr.TCall).name.text)
		sAssertEqual(
			"contents are different",
			expected.sortBy(_.name.text),
			actual.sortBy(_.name.text)
		)

		sAssertEqual(
			"ordering is different",
			expected,
			actual
		)
	}

	def labels: List[String]

	def loader: String

	def testDefined(): Unit = {
		smol
		treeCode
		header
		labels
		loader
		expandedEnums
	}

	def testParse(): Unit = {
		parser.module()
	}

	def testCompile(): Unit = {

		val actual: SmolIr.Module =
			Compiler(parser.module())

		val expected: SmolIr.Module =
			treeCode

		sAssertEqual(
			expected,
			actual
		)
	}

	private def parser =
		new SmolIrParser(
			new CommonTokenStream(
				new SmolIrLexer(
					new ANTLRInputStream(
						new StringReader(smol)
					).setName(getName)
				).handleErrors(failError)
			)
		).handleErrors(failError)

	def failError(recognitionException: RecognitionException, message: String, line: Int): Unit =
		fail(
			s"in $getName; `$message` @ $line"
		)

	def testHeader(): Unit = {

		val actual: String =
			new Header(treeCode)(new StringWriter()).toString.replaceAll("[\r \t]*\n", "\n")

		val expected: String =
			header.replaceAll("[\r \t]*\n", "\n")

		assertEquals(
			expected,
			actual
		)
	}

	def testLoader(): Unit = {

		val actual: String =
			new Loader(treeCode)(new StringWriter())
				.toString
				.replaceAll("[\r \t]*\n", "\n")

		val expected: String =
			loader
				.replaceAll("[\r \t]*\n", "\n")

		assertEquals(
			expected,
			actual
		)
	}

	def testNames(): Unit = {

		sAssertEqual(
			s"name-strings don't match in $getName",
			labels,
			new Labels(new ByteArrayOutputStream())
				.strings(treeCode)
		)

		assertEquals(
			s"name-bytes don't match in $getName",
			labels.flatMap {
				name: String =>
					name.getBytes ++ Array[Byte](0)
			}.toArray.toList,
			new Labels(new ByteArrayOutputStream())(treeCode)
				.toByteArray.toList
		)
	}
}
