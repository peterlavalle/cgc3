package peterlavalle.cgc3

import org.gradle.api.tasks.TaskAction

import scala.beans.BeanProperty

class TaskCompile extends ATask {

	@BeanProperty
	var pattern: String = ".+\\.(c|cpp)"

	def args: Seq[String] =
		getProject.rootExt[CGC3.Root].gccArgs.split(" ")

	getProject.ext[CGC3].yggdrasil.run("cpp", "obj") {
		GCC.compile(args, getProject.ext[CGC3].yggdrasil, pattern, getProject.rootExt[CGC3.Root].ifVerbose, println, errorln)
	}

	@TaskAction
	def compileAction(): Unit = {
		getProject.ext[CGC3].yggdrasil.sources("obj")
	}
}
