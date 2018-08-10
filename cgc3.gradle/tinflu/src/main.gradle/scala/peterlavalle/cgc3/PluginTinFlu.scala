package peterlavalle.cgc3

import java.io.{File, FileInputStream}

import org.gradle.api.Project
import peterlavalle.OverWriter

import scala.beans.BeanProperty
import scala.io.Source

object PluginTinFlu {

	class Extension {

		@BeanProperty
		var iterations: Int = 14

		@BeanProperty
		var blockSplittingMax: Int = 256
	}

	lazy val (begin, close) = {

		val List(begin, close) =
			Source
				.fromInputStream(getClass.getResourceAsStream("PluginTinFlu.TIN.c"))
				.mkString
				.split("#TIN#")
				.toList
				.map((_: String).trim.split("[\r \t]*\n").toList)

		val header = "\t#define TIN_#TIN#(NAME, SIZE, SPAN)"

		(
			begin.foldLeft(header.replace("#TIN#", "BEGIN"))((_: String) + " /\n\t\t" + (_: String)) + "\n\n",
			close.foldLeft(header.replace("#TIN#", "CLOSE"))((_: String) + " /\n\t\t" + (_: String)) + "\n\n"
		)
	}

}

class PluginTinFlu extends TPlugin {

	override def apply(project: Project): Unit = {
		require(null != project.ext[APlugin.ACompilePlugin])

		project.getRootProject.extend[PluginTinFlu.Extension]("tin")

		project.ext[CGC3].yggdrasil.run("tin", "cpp") {
			(out: File, ygg: Yggdrasil, from: File, path: String) =>

				// listing file
				val sources: Stream[String] = ygg.sourceTransitive("tin").toStream.flatMap(_.toList.map(_._2)).distinct
				new OverWriter(out / "tin.h")
					.appund("\n")
					.appund("#pragma once\n")
					.appund("\n")
					.appund("#include <stdint.h>\n")
					.appund("#ifdef tin_c\n")
					.appund("// i need some extra headers for the body\n")
					.appund("#include <assert.h>\n")
					.appund("#include <memory.h>\n")
					.appund("#include <string.h>\n")
					.appund("#include <tinfl.h>\n")
					.appund("#endif // tin_c\n")
					.appund("\n")
					.appund("// simply all-the-things\n")
					.appund("#ifdef tin_all\n")
					.appund(sources) {
						src: String =>
							"#include " + '"' + src + '"' + "\n"
					}
					.appund("#endif // tin_all\n")
					.appund("\n")
					.appund("#ifdef __cplusplus\n")
					.appund("extern \"C\" {\n")
					.appund("#endif\n")
					.appund("\n")
					.appund("void tin(const char*, void*, void(*)(void*, const char*, const size_t, const void*));\n")
					.appund("extern const size_t tin_count;\n")
					.appund("size_t tin_loaded(void);\n")
					.appund("\n")
					.appund("// predefined loading thing\n")
					.appund("#ifdef tin_c\n")
					.appund(s"const size_t tin_count = ${sources.size};\n")
					.appund("static size_t _tin_loaded = 0;\n")
					.appund("size_t tin_loaded(void) { return _tin_loaded; }\n")
					.appund("void tin(const char* filename, void* userdata, void(*callback)(void*, const char*, const size_t, const void*))\n")
					.appund("{\n")
					.appund("\tsize_t size;\n")
					.appund(PluginTinFlu.begin)
					.appund(PluginTinFlu.close)
					.appund(sources) {
						src: String =>
							"\t\t#include " + '"' + src + '"' + "\n"
					}
					.appund("}\n")
					.appund("#endif // tin_c\n")
					.appund("#ifdef __cplusplus\n")
					.appund("}\n")
					.appund("#endif\n")
					.closeFile

				val src: File = from / path
				val zip: File = out / (path + ".h")

				if (zip.exists() && zip.lastModified() > src.lastModified()) {
					project.rootExt[CGC3.Root].ifVerbose {
						println(s"reusing old $path")
					}
				} else {
					project.rootExt[CGC3.Root].ifVerbose {
						println(s"compressing $path")
					}

					val srcData: Array[Byte] =
						new FileInputStream(src).toArrays.flatten.toArray

					val zipData: Stream[Byte] =
						Compress.zopfli(
							srcData,
							project.rootExt[PluginTinFlu.Extension].iterations,
							project.rootExt[PluginTinFlu.Extension].blockSplittingMax
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
}
