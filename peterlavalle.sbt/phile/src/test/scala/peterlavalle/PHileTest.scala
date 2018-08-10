package peterlavalle

import java.io.File

import org.junit.Assert._

class PHileTest extends ATestCase {

	def testAgainstZip(): Unit = {
		val src: PHile =
			PHile.ofFolder(
				new File("src").getAbsoluteFile
			)

		val zip: PHile =
			PHile.ofZip(src.toZip)

		assertTrue(src matches zip)
		assertTrue(src matches src)
		assertTrue(zip matches zip)
		assertTrue(zip matches src)
	}

	def testZipZip(): Unit = {
		val fooBar: PHile =
			fromArchive("foo-bar.zip")

		val bar: PHile =
			fromArchive("bar.zip")

		val both: PHile =
			bar.link(fooBar)

		val contents: List[(String, String)] =
			List(
				"bar.txt" -> "bar",
				"foo.txt" -> "foo-bar",
				"sub/bar.txt" -> "bar",
				"sub/foo.txt" -> "foo-bar"
			)

		sAssertEqual(
			contents.map((_: (String, String))._1),
			both.list.toList.sorted
		)

		sAssertEqual(
			contents,
			both.flat.toList.map {
				case (name, data) =>
					(name, new String(data))
			}
		)
	}

	def testPizPiz(): Unit = {
		val fooBar: PHile =
			fromArchive("foo-bar.zip")

		val bar: PHile =
			fromArchive("bar.zip")

		val both: PHile =
			fooBar link bar

		val contents: List[(String, String)] =
			List(
				"bar.txt" -> "foo-bar",
				"foo.txt" -> "foo-bar",
				"sub/bar.txt" -> "foo-bar",
				"sub/foo.txt" -> "foo-bar"
			)

		sAssertEqual(
			contents.map((_: (String, String))._1),
			both.list.toList.sorted
		)

		sAssertEqual(
			contents,
			both.flat.toList.map {
				case (name, data) =>
					(name, new String(data))
			}
		)
	}

	def testRename(): Unit = {

		val foo: PHile =
			fromArchive("foo.zip")

		val done: PHile =
			foo.rename("foo/(.+)", "$1.cpp")

		val contents: List[(String, String)] =
			List(
				"foo.txt.cpp" -> "foo"
			)

		sAssertEqual(
			contents.map((_: (String, String))._1),
			done.list.toList.sorted
		)

		sAssertEqual(
			contents,
			done.flat.toList.map {
				case (name, data) =>
					(name, new String(data))
			}
		)
	}

	def testRenameRename(): Unit = {

		val archive: PHile =
			fromArchive("whippet.zip")
				.rename("src/main/cgc/(.*.cpp)", "src/$1")
				.rename("src/main/cgc/(.*.hpp)", "inc/$1")
				.rename("src/test/cgc/(.*.(c|h)pp)", "test/$1")


		val contents: List[String] =
			List(
				"inc/hanoi.hpp",
				"inc/whippet.hpp",
				"inc/whippet.inline.hpp",
				"src/whippet-component.cpp",
				"src/whippet-entity.cpp",
				"src/whippet-porcelain.cpp",
				"src/whippet-system.cpp",
				"src/whippet-universe.cpp",
				"test/whippet-test.cpp"
			)

		sAssertEqual(
			contents,
			archive.flat.toList.map {
				case (name, _) =>
					name
			}
		)
	}

	def fromArchive(name: String): PHile =
		PHile.ofZip(
			getClass.getResourceAsStream(name)
		)
}
