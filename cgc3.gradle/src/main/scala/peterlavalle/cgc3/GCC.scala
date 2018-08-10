package peterlavalle.cgc3

import java.io.File

import peterlavalle.TreeFolder

object GCC {


	def assemble(args: Seq[String], yggdrasil: Yggdrasil, name: String, ifVerbose: (=> Unit) => Unit, outline: String => Unit, errline: String => Unit): Yggdrasil.Assembler = {
		(out: File, _: Iterable[TreeFolder]) =>

			val bin: File =
				out / name.exe

			val obj: Seq[File] =
				yggdrasil.sourceTransitive("obj").flatten.toSeq.map {
					case (root, path) =>
						require(path.endsWith(".o") || path.endsWith(".d"))
						if (path.endsWith(".o"))
							root.root / path
						else {
							""
						}
				}.filterNot((_: Object).isInstanceOf[String]).map((_: Object).asInstanceOf[File])

			out.Shell("g++")
				.newArgs(args: _ *)
				.newArgs("-o", bin)
				.newArgs(obj: _ *)
				.invoke(o => outline(o), e => errline(e)) match {
				case 0 =>
					ifVerbose {
						outline("built")
					}

				case r =>
					throw new Exception {
						s"linking failed with r = $r, out=`${bin.AbsolutePath}` and:" match {
							case left: String =>
								args.foldLeft(left)(_ + "\n\targ: >" + _ + "<") match {
									case left: String =>
										obj.foldLeft(left)(_ + "\n\tobj: >" + _.AbsolutePath + "<")
								}
						}
					}
			}
	}

	def compile(args: Seq[String], yggdrasil: Yggdrasil, pattern: String, ifVerbose: (=> Unit) => Unit, outline: String => Unit, errline: String => Unit): Yggdrasil.Compiler =
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
					ifVerbose {
						outline(path + " is up to date")
					}
				else
					out
						.Shell("g++")
						.newArg("-MMD") // generate .d files for time checks
						.newArgs(args: _ *)
						.newArgs("-c", "-o", obj)
						.allArgs(ygg.sourceTransitive("cpp").filter((_: TreeFolder).root.exists())) {
							inc: TreeFolder =>
								"-I" + inc.root.AbsolutePath
						}
						.newArg(src)
						.invoke(
							(o: String) => outline(path + ";" + o),
							(e: String) => errline(path + "!" + e)
						) match {
						case 0 =>
							ifVerbose {
								outline(path + " compiled successfully!")
							}

						case r =>
							sys.error(s"failed compile!, r = $r\n\t${src.AbsolutePath}")
					}
			}


}
