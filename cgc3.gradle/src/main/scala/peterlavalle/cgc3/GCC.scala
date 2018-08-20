package peterlavalle.cgc3

import java.io.File

import org.codehaus.plexus.util.cli.Commandline
import peterlavalle.TreeFolder

object GCC {

	def assemble(plonk: Plonk)(args: Seq[String], yggdrasil: Yggdrasil, name: String): Yggdrasil.Assembler = {
		(out: File, _: Iterable[TreeFolder]) =>

			val bin: File = out / name.exe

			val obj: Seq[File] =
				yggdrasil.sourceTransitive("obj").flatten.toSeq.distinctBy(_._2).map {
					case (root, path) =>
						require(path.endsWith(".o") || path.endsWith(".d"))
						if (path.endsWith(".o"))
							root.root / path
						else {
							""
						}
				}.filterTo[File].toSeq

			plonk.ifVerbose {
				plonk.outline(
					obj.foldLeft(s"linking ${bin.AbsolutePath} from:")((_: String) + "\n\t" + (_: File).AbsolutePath)
				)
			}

			val commandLine: Commandline = out.Shell("g++")
				.newArgs(args: _ *)
				.newArgs("-o", bin)
				.newArgs(obj: _ *)

			val start: Long = System.currentTimeMillis()

			def sec: String =
				((System.currentTimeMillis() - start) * 0.001).toString.split("\\.").toList match {
					case List(full: String, partial: String) =>
						full + "." + partial.take(3) + "sec"
				}

			commandLine
				.text {
					case (0, out: Iterable[String], Nil) =>
						plonk.ifVerbose {
							out.foreach(l => plonk.outline(l))
						}
						plonk.outline(s"linking completed in $sec")

					case (r, out: Iterable[String], err: Iterable[String]) =>
						assume(null != bin, "How is `bin` null?")
						val message: String = s"linking failed with r = $r, out=`${bin.AbsolutePath}` and:" ~
							args ~ ((_: String) + "\n\t\targ: >" + (_: String) + "<") ~
							obj ~ ((_: String) + "\n\t\tobj: >" + (_: File).AbsolutePath + "<") ~
							out ~ ((_: String) + "\n\t\tout; " + (_: String)) ~
							err ~ ((_: String) + "\n\t\terr! " + (_: String))

						message.split("[\r \t]*\n").foreach(l => plonk.errline(l))
						throw new Exception(message)
				}
	}

	def compile(plonk: Plonk)(args: Seq[String], yggdrasil: Yggdrasil, pattern: String): Yggdrasil.Compiler =
		(out: File, ygg: Yggdrasil, from: File, path: String) =>
			if (path matches pattern) {

				lazy val obj: File = out / s"$path.o"
				lazy val dep: File = out / s"$path.d"
				lazy val src: File = from / path

				def srcAge: Long =
					dep
						.sourceLines
						.tail
						.map((_: String).dropRight(1).trim)
						.map(new File(_: String).lastModified())
						.foldLeft(src.lastModified())(Math.max)

				def objAge: Long = obj.lastModified()

				if (obj.exists() && dep.exists() && objAge > srcAge)
					plonk.ifVerbose {
						plonk.outline(path + " is up to date")
					}
				else {

					// we want newest first
					// https://stackoverflow.com/questions/41437648/specifying-order-in-gcc-and-g-include-and-lib-paths#41440237
					val inc: Seq[File] =
					ygg
						.sourceTransitive("cpp")
						.filter((_: TreeFolder).root.exists())
						.toList
						.map((_: TreeFolder).root)

					plonk.ifVerbose {
						plonk.outline(
							inc.foldLeft(s"compiling ${src.AbsolutePath} with:")((_: String) + "\n\t" + (_: File).AbsolutePath)
						)
					}

					out
						.Shell("g++")
						.newArg("-MMD") // generate .d files for time checks
						.newArgs(args: _ *)
						.newArgs("-c", "-o", obj)
						.allArgs(inc) {
							inc: File =>
								"-I" + inc.AbsolutePath
						}
						.newArg(src)
						.invoke(
							(o: String) => plonk.outline(path + ";" + o),
							(e: String) => plonk.errline(path + "!" + e)
						) match {
						case 0 =>
							plonk.ifVerbose {
								plonk.outline(path + " compiled successfully!")
							}

						case r =>
							sys.error(s"failed compile!, r = $r\n\t${src.AbsolutePath}")
					}
				}
			}
}
