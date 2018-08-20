package peterlavalle.cgc3

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, FileOutputStream}
import java.net.URL
import java.util.zip.{ZipEntry, ZipFile}

import peterlavalle.UrlCache

import scala.collection.immutable.Stream.Empty
import scala.util.matching.Regex

/**
	* Someday ... this'll download all coffee-script releases into my regression test/s so that I can work out which one(s) function in DukTape
	*
	* ... and if that doesn't help; i'll apply something like babel as an optional
	*/
object CoffeeScriptDownload extends App {
	val urlCache = new UrlCache(new File("out") / "coffee-cache")
	val into =
		new File("out").ParentFile / "../coffeeskript.cgc/coffeescript-test-regress/src"

	def versionLinks: Stream[(String, URL)] = {

		val rName: Regex = "\\s*\"name\"\\s*:\\s*\"([^\"]+)\"\\s*\\s*" r

		val rLink: Regex = "\\s*\"zipball_url\"\\s*:\\s*\"([^\"]+)\"\\s*\\s*" r

		def consume(todo: Stream[String]): Stream[(String, URL)] =
			todo match {
				case rName(name) #:: rLink(link) #:: tail =>
					(name, new URL(link)) #:: consume(tail)

				case _ #:: tail => consume(tail)

				case _ => Empty
			}

		consume {
			urlCache("https://api.github.com/repos/jashkenas/coffeescript/tags")
				.sourceLines
				.flatMap(_.split(","))
				.map(_.dropWhile('[' == _).dropWhile('{' == _))
		}
	}

	versionLinks.foreach {
		case (version: String, link: URL) =>

			// load it into a local zip file
			val local: ZipFile = urlCache.zip(link)

			// find the biggest coffee-???.js file and data
			val Stream(Some(data: Array[Byte])) =
				local.entries().toStream.filter((_: ZipEntry).getName.matches("(.+/)*coffee[^/]+\\.js")).map {
					next: ZipEntry =>

						val nextData: Array[Byte] =
							(new ByteArrayOutputStream() << local.getInputStream(next))
								.toByteArray

						if (new String(nextData, "UTF-8").contains(s"CoffeeScript Compiler v$version"))
							Some(nextData)
						else
							None
				}.filter(_.nonEmpty)

			(new FileOutputStream(into / s"coffeescript-$version.js") << new ByteArrayInputStream(data))
				.close()
	}
}
