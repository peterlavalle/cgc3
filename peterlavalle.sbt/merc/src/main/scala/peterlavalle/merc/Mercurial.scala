package peterlavalle.merc

import java.io.File

object Mercurial {

	def open(root: File): Mercurial.TRepository =
		new Mercurial.TRepository {
			val self: TRepository = this

			override def active: Mercurial.TRevision =
				root.Shell("hg", "log", "-r.", "--template", "{node}").text {
					case (0, out, Nil) =>
						new Mercurial.TRevision {
							val node: String = out.foldLeft("")((_: String) + (_: String))

							val repository: TRepository = self

							lazy val branch: String =
								root.Shell("hg", "log", "-r.", "--template", "{branch}").text {
									case (0, outBranch, Nil) =>
										outBranch.foldLeft("")((_: String) + (_: String))
								}
						}
				}

			override def heads: Iterable[TRevision] =
				root.Shell("hg", "heads", "-r.", "--template", "#{node}#").text {
					case (0, out, Nil) =>

						// this will show if super-magic works - if so; we cap map out instead of split
						if (out.forall(_.matches("#\\w+#")))
							System.err.println("matchin super magic; works")
						else
							System.err.println("matchin super magic; fails" + out)

						out.foldLeft("")((_: String) + (_: String)).split("#").toStream.filterNot("" == _).map {
							data: String =>
								new Mercurial.TRevision {

									val repository: TRepository = self

									lazy val branch: String =
										root.Shell("hg", "log", "-r.", "--template", "{branch}").text {
											case (0, outBranch, Nil) =>
												outBranch.foldLeft("")((_: String) + (_: String))
										}
									override val node: String = data
								}
						}
				}
		}

	trait TRevision {
		val node: String
		val repository: TRepository

		def branch: String
	}

	trait TRepository {
		def heads: Iterable[TRevision]

		def active: Mercurial.TRevision
	}

}
