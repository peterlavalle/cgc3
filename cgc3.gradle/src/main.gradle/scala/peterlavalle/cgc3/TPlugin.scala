package peterlavalle.cgc3

import org.gradle.api.{GradleException, Plugin, Project}

abstract class TPlugin extends Plugin[Project] with TGradle {


	final def require(condition: Boolean): Unit =
		require(condition, null)

	final def require(condition: Boolean, message: => String): Unit =
		if (!condition)
			failure(message)

	final def failure(message: String = null): Unit =
		throw new GradleException(
			message match {
				case null => s"failure in ${getClass.getName}"
				case message: String => getClass.getName + "! " + message
			}
		)

}
