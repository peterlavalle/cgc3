package peterlavalle.cgc3

import org.gradle.api.tasks.TaskAction

import scala.beans.BeanProperty

class TaskCompile extends ATask {

	@BeanProperty
	var pattern: String = ".+\\.(c|cpp)"

	def args: Seq[String] =
		getProject.rootExt[CGC3.Root].cgc3GCCArgs.split(" ")

	getProject.ext[CGC3].yggdrasil.run("cpp", "obj") {
		GCC.compile(plonk)(args, getProject.ext[CGC3].yggdrasil, pattern)
	}

	@TaskAction
	def compileAction(): Unit = {
		getProject.ext[CGC3].yggdrasil.sources("obj")
	}
}
