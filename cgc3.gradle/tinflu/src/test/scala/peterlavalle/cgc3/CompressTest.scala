package peterlavalle.cgc3

import java.io.{ByteArrayInputStream, File, FileInputStream}
import java.util.zip.{Inflater, InflaterInputStream}

import org.junit.Assert._
import peterlavalle.ATestCase

import scala.io.Source

class CompressTest extends ATestCase {
	val coffeeScriptFile = new File("foo").ParentFile / "../examples/coffeescript/coffeescript/src/coffeescript-2.3.1.js"
	val List(coffeeScriptSource: String) =
		coffeeScriptFile
			.sourceLines.filter("" != (_: String))
			.toList
	val coffeeScriptBytes: Array[Byte] = new FileInputStream(coffeeScriptFile).toArrays.toArray.flatten

	def testLoading(): Unit = {
		assertEquals(
			coffeeScriptSource,
			Source.fromBytes(coffeeScriptBytes).mkString
		)
	}

	def testLengths(): Unit = {
		assertEquals(
			coffeeScriptFile.length(),
			coffeeScriptBytes.length
		)
	}

	def testLengthsHard(): Unit = {
		assertEquals(397121, coffeeScriptBytes.length)
		assertEquals(397121, coffeeScriptFile.length())
	}

	def testCompressDecompressCoffee(): Unit =
		if (true)
			System.err.println("TODO; actually test this")
		else {

			val deflatedCoffee: Iterable[Byte] =
				Compress.zopfli(
					coffeeScriptBytes,
					14,
					256
				)

			val inflater = new Inflater()


			val stream = new ByteArrayInputStream(deflatedCoffee.toArray)

			assertEquals(
				coffeeScriptSource,
				Source
					.fromInputStream(new InflaterInputStream(stream))
					.mkString
			)
		}

}
