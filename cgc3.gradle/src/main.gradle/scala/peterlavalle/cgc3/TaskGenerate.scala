package peterlavalle.cgc3

import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import peterlavalle._

class TaskGenerate extends ATask {
	getProject.ext[CGC3].yggdrasil.src("cpp", getProject.getProjectDir / "src")

	@TaskAction
	def generateAction(): Unit = {
		getProject.ext[CGC3].yggdrasil.sources("cpp")
	}

	// generate should re-trigger the solution task
	TaskBus(this) {
		them: Task =>
			if (them.isInstanceOf[PluginSolution.TaskSolution])
				dependsOn(them)
	}
}
