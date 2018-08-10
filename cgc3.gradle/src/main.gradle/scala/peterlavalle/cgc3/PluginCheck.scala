package peterlavalle.cgc3

import org.gradle.api.Project

class PluginCheck extends APlugin.ACompilePlugin {
	override def setup(project: Project): Unit = {
		val generate: TaskGenerate = project.createTask[TaskGenerate]
		val compile: TaskCompile = project.createTask[TaskCompile]
		val link: TaskAssemble = project.createTask[TaskAssemble]
		val test: TaskCheck = project.createTask[TaskCheck]

		compile.dependsOn(generate)
		link.dependsOn(compile)
		test.dependsOn(link)
	}
}
