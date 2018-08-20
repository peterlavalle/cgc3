package peterlavalle.cgc3

class PluginBinary extends APlugin.ACompilePlugin {
	setup {
		val generate: TaskGenerate = project.createTask[TaskGenerate]
		val compile: TaskCompile = project.createTask[TaskCompile]
		val link: TaskAssemble = project.createTask[TaskAssemble]

		compile.dependsOn(generate)
		link.dependsOn(compile)
	}
}
