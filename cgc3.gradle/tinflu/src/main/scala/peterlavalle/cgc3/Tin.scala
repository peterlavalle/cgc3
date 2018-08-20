package peterlavalle.cgc3

import java.io.{File, FileInputStream}

import peterlavalle.OverWriter

import scala.io.Source

object Tin {
	lazy val lines: Stream[String] =
		Source.fromInputStream(getClass.getResourceAsStream("PluginTinFlu.TIN.c"))
			.mkString
			.split("[\r \t]*\n")
			.toStream

	def Flue(plonk: Plonk)(iterations: Int, blockSplittingMax: Int): Yggdrasil.Compiler = {
		(out: File, ygg: Yggdrasil, from: File, path: String) =>

			// listing file
			val sources: Stream[String] = ygg.sourceTransitive("tin").toStream.flatMap(_.toList.map(_._2)).distinct
			new OverWriter(out / "tin.h")
				.appund(
					lines.flatMap {
						case "#TIN#" =>
							sources.map {
								src: String =>
									"#include \"" + src + ".h\""
							}
						case line =>
							List(
								line.replace("#LEN#", sources.size.toString)
							)
					}
				) {
					line: String =>
						line + "\n"
				}
				.closeFile

			val src: File = from / path
			val zip: File = out / (path + ".h")

			if (zip.exists() && zip.lastModified() > src.lastModified()) {
				plonk.ifVerbose {
					plonk.outline(s"reusing old $path")
				}
			} else {
				plonk.ifVerbose {
					plonk.outline(s"compressing $path")
				}

				val srcData: Array[Byte] =
					new FileInputStream(src).toArrays.flatten.toArray

				val zipData: Stream[Byte] =
					Compress.zopfli(
						srcData,
						iterations,
						blockSplittingMax
					).toStream

				new OverWriter(zip)
					.appund("\nTIN_BEGIN(\n")
					.appund(s"\t${'"' + path + '"'}, // name\n")
					.appund(s"\t${srcData.length}, // src.size\n")
					.appund(s"\t${zipData.size} // zip.size\n")
					.appund(s")\n")
					.appund(zipData.grouped(14)) {
						line: Stream[Byte] =>
							"\t\t" + ByeCoder(line).reduce((_: String) + ", " + (_: String)) + ",\n"
					}
					.appund("TIN_CLOSE(\n")
					.appund(s"\t${'"' + path + '"'}, // name\n")
					.appund(s"\t${srcData.length}, // src.size\n")
					.appund(s"\t${zipData.size} // zip.size\n")
					.appund(s")\n")
					.closeFile
					.touch
			}
	}
}
