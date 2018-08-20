package peterlavalle.cgc3

import org.gradle.api.{GradleException, Project, Task}

trait TGradle extends PGradleProject {

	lazy val plonk: Plonk = {
		def project: Project =
			this match {
				case task: Task => task.getProject
				case once: TProjectSetOnce => once.project

				case fail =>
					sys.error(
						s"trying to find project on ${fail.getClass.getName}"
					)
			}

		new Plonk {
			override def ifVerbose(action: => Unit): Unit = {
				project.rootExt[CGC3.Root].ifVerbose(action)
			}

			override def outline(o: => String): Unit = println(o)

			override def errline(e: => String): Unit = errline(e)
		}
	}

	final def require(condition: Boolean): Unit =
		require(condition, null)

	final def require(condition: Boolean, message: => String): Unit =
		if (!condition)
			try {
				failure(message)
			} catch {
				case g: GradleTaskException =>
					throw g.stackChop(1)
			}

	final def failure(message: String = null): Unit =
		throw new GradleTaskException(
			message match {
				case null => s"failure in $getPath"
				case message: String => getPath + "! " + message
			}
		).stackChop(1)

	final def println(o: Any): Unit =
		o.toString.split("[\r \t]*\n").foreach {
			line: String =>
				System.out.println(getPath + "; " + line, "")
		}

	final def errorln(o: Any): Unit =
		o.toString.split("[\r \t]*\n").foreach {
			line: String =>
				System.out.println(getPath + "! " + line, "")
		}

	def getPath: String

	class GradleTaskException(message: String) extends GradleException(message: String) {
		def stackChop(i: Int): GradleTaskException = {
			setStackTrace(getStackTrace.drop(i))
			this
		}
	}

}

object TGradle {

	trait TTask {
		def setup(project: Project)
	}

}
