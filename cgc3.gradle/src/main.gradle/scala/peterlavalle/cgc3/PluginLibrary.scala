package peterlavalle.cgc3

import org.gradle.api.Project

class PluginLibrary extends APlugin.ACompilePlugin {

	override def setup(project: Project): Unit = {
		val generate: TaskGenerate = project.createTask[TaskGenerate]
		val compile: TaskCompile = project.createTask[TaskCompile]

		compile.dependsOn(generate)

	}

}
