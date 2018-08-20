package peterlavalle.cgc3

class PluginLibrary extends APlugin.ACompilePlugin {
	setup {
		val generate: TaskGenerate = project.createTask[TaskGenerate]
		val compile: TaskCompile = project.createTask[TaskCompile]

		compile.dependsOn(generate)
	}
}
