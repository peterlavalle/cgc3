package peterlavalle.cgc3

import java.io.File

import peterlavalle.ATestCase
import peterlavalle.TreeFolder._

class YggdrasilTest extends ATestCase with TTestCase {

	lazy val goal: File = new File("examples") / "basic"
	lazy val build: File = goal / "build" / "test" / getName

	def testFullGoal(): Unit = {

		def docpp(name: String): Yggdrasil = {
			val yggdrasil = Yggdrasil((p: String) => build / name / p EnsureExists, ???)

			yggdrasil.src("cpp", goal / name / "src")

			def outline(s: String) = System.out.println(getName + ":" + name + "/" + s)

			def errline(s: String) = System.err.println(getName + ":" + name + "/" + s)

			yggdrasil.run("cpp", "obj")(GCC.compile(Seq("-g"), yggdrasil, ".+\\.(c|cpp)", action => action, outline, errline))

			yggdrasil
		}

		val shared =
			docpp("shared")

		def doexe(name: String): Yggdrasil = {
			val code = docpp(name)

			// make the exe depend upon the shared code
			code lib shared


			def outline(s: String): Unit = System.out.println(getName + ":" + name + ".link/" + s)

			def errline(s: String): Unit = System.err.println(getName + ":" + name + ".link/" + s)

			// make the exe link
			code.asm("obj") {
				GCC.assemble(
					Seq("-g"),
					code,
					name,
					action => action,
					outline,
					errline
				)
			}

			code
		}

		val binary = doexe("binary")

		val checks: Yggdrasil = doexe("checks")


		// add bits
		checks.eat("obj") {
			dir: File =>

				val out = dir / ("checks" exe)

				require(out.exists())
				require(out.makeExecutable.canExecute)
				build.Shell(out).text {
					//dir.Shell("checks" exe).text {
					case (0, _, Nil) =>
						println("checks has passed checks")

					case (r, out, err) =>
						failed(
							(dir ** (".+": String)).foldLeft(
								"something went ... wrong ... when executing the check program")(_ + "\n\t" + _)
						)
				}
		}

		// make it!
		sAssertEqual(
			List("checks" exe), {
				val file: File =
					try {
						checks.compile("obj")
					} catch {
						case e: Throwable =>
							System.err.println(s"checks.compile('obj') failed with `${e.getMessage}`")
							throw e
					}

				try {
					file.list().toList
				} catch {
					case e: Throwable =>
						System.err.println(s"checks.compile('obj').list().toList failed with `${e.getMessage}`")
						throw e
				}
			}
		)



		// can we also make it?
		assertEquals(
			List("binary" exe),
			binary.compile("obj").list().toList
		)
	}
}
