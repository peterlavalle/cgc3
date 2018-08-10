package peterlavalle

import java.io.{File, InputStream}

import scala.collection.immutable.Stream.Empty

package object cgc3
	extends peterlavalle.TPackage {

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
		def touch: Unit = {
			(if (file.absent)
				new OverWriter(file).closeFile
			else
				file).setLastModified(System.currentTimeMillis())
		}

		def absent: Boolean = !file.exists

		def makeExecutable: File = ifNotExecutable(f => List("chmod", "+=rwx", f.AbsolutePath))

		def ifNotExecutable(command: File => Seq[String]): File = {
			require(file.exists())
			if (file.canExecute)
				file
			else
				(command apply file) toList match {
					case command :: head :: tail =>
						file.ParentFile.Shell(command, head, tail: _ *)
							.text {
								case (0, Nil, Nil) =>
									file
							}
				}
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
