package peterlavalle.cgc3


import java.io.File
import java.util

import org.gradle.api.{GradleException, Project}
import peterlavalle.TaskBus

import scala.beans.BeanProperty

/**
	* singleton.
	*/
class CGC3(val project: Project, mkdir: String => File) extends TGradle {

	// TODO; punch the not-root stuff out to a subclass

	require(
		(project == project.getRootProject) == isInstanceOf[CGC3.Root]
	)

	lazy val yggdrasil: Yggdrasil = Yggdrasil(mkdir, sys.error("how get age of config"))

	final def src(what: Object): Unit =
		what match {
			case whom: util.LinkedHashMap[String, File] =>
				require(1 == whom.size())
				val List(key: String) = whom.keys.toList
				require(whom(key).isInstanceOf[File])
				src(whom, ".+")
		}

	final def src(whom: util.LinkedHashMap[String, File], pattern: String): Unit = {
		require(1 == whom.size())
		val List(key: String) = whom.keys.toList
		val List(root: File) = whom.values().toList
		yggdrasil.src(key, (root, pattern))
	}

	def lib(project: Object): Unit =
		project match {
			case project: Project =>
				TaskBus.consume[CGC3](project) {
					bean: CGC3 =>
						if (project == bean.project) {
							val lib: CGC3 = project.ext[CGC3]

							require(null != lib, "this implies a circular dependency")

							require(
								lib == bean,
								s"was expecting cgc3 from ${project.getName} but got from ${lib.project.getName}"
							)

							require(null != yggdrasil)

							require(null != lib.yggdrasil)

							yggdrasil.lib(lib.yggdrasil)
						}
				}

			case _ =>
				throw new GradleException(s"I don't know how to 'lib' the object of class ${project.getClass.getName}")
		}

	protected def add(name: String, thing: Object): Unit = {
		sys.error("what do how, !")
	}
}

object CGC3 {

	class Root(project: Project, mkdir: String => File) extends CGC3(project, mkdir) {

		// TODO; find a way to remove this
		override lazy val yggdrasil: Yggdrasil =
			sys.error("this shouldn't exist on root")
		@BeanProperty
		var gccArgs: String =
			System.getProperty("cgc3-gcc-args", "-g") match {
				case escaped if escaped.length >= 2 && escaped.startsWith("'") && escaped.endsWith("'") => escaped.tail.dropRight(1)
				case okay => okay
			}
		@BeanProperty
		var verbose: Boolean = false
		@BeanProperty
		var daemon: Boolean = false

		def ifVerbose(action: => Unit): Unit =
			if (verbose)
				action

		// TODO; find a way to remove this
		override def lib(project: Object): Unit =
			sys.error("this shouldn't happen on root")
	}

}
