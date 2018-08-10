package peterlavalle.cgc3

import org.gradle.api.{DefaultTask, GradleException}

abstract class ATask extends DefaultTask with TGradle {
	setDescription {
		getClass
			.getName
			.replaceAll("_Decorated$", "")
			.replaceAll("_decorated$", "")
	}
	setGroup("cgc3")

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

	class GradleTaskException(message: String) extends GradleException(message: String) {
		def stackChop(i: Int): GradleTaskException = {
			setStackTrace(getStackTrace.drop(i))
			this
		}
	}
}
