package peterlavalle

import java.io.{File, InputStream}

import scala.collection.immutable.Stream.Empty
import scala.reflect.ClassTag

package object cgc3
	extends peterlavalle.TPackage {

	implicit class pObject2[A](o: A) {
		def asEither[V, L, R](l: L => V, r: R => V)(implicit lTag: ClassTag[L], rTag: ClassTag[L]): V = {

			val lClass: Class[L] = lTag.runtimeClass.asInstanceOf[Class[L]]
			val rClass: Class[R] = rTag.runtimeClass.asInstanceOf[Class[R]]

			if (lClass.isInstance(o))
				l(lClass.cast(o))
			else {
				require(rClass.isInstance(o))
				r(rClass.cast(o))
			}
		}
	}

	implicit class pString2cgc(name: String) {
		def exe: String =
			osnamearch {
				case ("windows", arch@("amd64" | "x86")) =>
					s"$name.$arch.exe"

				case ("linux", arch@("amd64" | "x86")) =>
					s"$name.$arch.lin"

				case ("mac", arch@"x86_64") =>
					s"$name.$arch.mac"

				case unknown =>
					throw new Exception(s"I don't know what name to use for os.name+arch = $unknown")
			}
	}

	def osnamearch[O](action: (String, String) => O): O =
		action(System.getProperty("os.name").takeWhile(' ' != (_: Char)).toLowerCase, System.getProperty("os.arch").toLowerCase)

	implicit class pFile3(file: File) {
		def touch: File = {
			(file.absent ?/ new OverWriter(file).closeFile :/ file)
				.setLastModified(System.currentTimeMillis())
			file.getAbsoluteFile
		}

		def absent: Boolean = !file.exists

		def makeExecutable: File = ifNotExecutable(f => List("chmod", "+=rwx", f.AbsolutePath))

		def nonExecute: Boolean = !file.canExecute

		def ifNotExecutable(command: File => Seq[String]): File = {
			require(file.exists())
			nonExecute ?/ ((command apply file) toList match {
				case command :: head :: tail =>
					file.ParentFile.Shell(command, head, tail: _ *)
						.text {
							case (0, Nil, Nil) =>
								file
						}
			}) :/ file
		}
	}

	implicit class pInputStream[I <: InputStream](stream: I) {
		def toArrays: Stream[Array[Byte]] = toArrayOf(32)

		def toArrayOf(size: Int): Stream[Array[Byte]] = {
			val bytes: Array[Byte] = Array.ofDim[Byte](size)

			stream.read(bytes) match {
				case -1 =>
					stream.close()
					Empty

				case read =>
					(if (read != size)
						bytes.take(read)
					else
						bytes) #:: toArrayOf(size + 4)
			}
		}
	}

}
