package peterlavalle.cgc3

class PluginCheck extends APlugin.ACompilePlugin {
	setup {
		val generate: TaskGenerate = project.createTask[TaskGenerate]
		val compile: TaskCompile = project.createTask[TaskCompile]
		val link: TaskAssemble = project.createTask[TaskAssemble]
		val test: TaskCheck = project.createTask[TaskCheck]

		compile.dependsOn(generate)
		link.dependsOn(compile)
		test.dependsOn(link)
	}
}
