package peterlavalle.cgc3

import java.io.{File, FileInputStream, FileOutputStream}

import org.gradle.api.tasks.TaskAction

class TaskAssemble extends ATask
	with VCodeGradle.TGDBLauncher
	with VCodeGradle.TVCTaskGCC
	with TCGCProblemMatcher {

	override lazy val target: File =
		getProject.getBuildDir / (getProject.getName exe)

	// perform the assembly
	getProject.ext[CGC3].yggdrasil.asm("obj") {
		GCC.assemble(
			args,
			getProject.ext[CGC3].yggdrasil,
			getProject.getName,
			getProject.rootExt[CGC3.Root].ifVerbose,
			println,
			errorln
		)
	}

	// copy the binary out
	getProject.ext[CGC3].yggdrasil.eat("obj") {
		dir: File =>
			val exe = dir / (getProject.getName exe)
			require(exe exists(), s"expected to find ${exe.AbsolutePath}")
			(new FileOutputStream(target.EnsureParent) << new FileInputStream(exe)).close()
	}

	def args: Seq[String] =
		getProject.rootExt[CGC3.Root].gccArgs.split(" ")

	@TaskAction
	def assemblyAction(): Unit = {
		getProject.ext[CGC3].yggdrasil.compile("obj")
	}
}
