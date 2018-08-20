package peterlavalle.cgc3

import java.io.{File, FileInputStream, FileOutputStream}

import org.gradle.api.tasks.TaskAction

class TaskAssemble extends ATask
	with VCodeGradle.TGDBLauncher
	with VCodeGradle.TVCTaskGCC // TODO; make a non-build task for this (et al) once the "polish build" thingie is in place
	with VCodeGradle.TVCTaskRoot
	with TCGCProblemMatcher {


	override lazy val target: File =
		getProject.getBuildDir / (getProject.getName exe)

	// perform the assembly
	getProject.ext[CGC3].yggdrasil.asm("obj") {
		GCC.assemble(plonk)(
			args,
			getProject.ext[CGC3].yggdrasil,
			getProject.getName
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
		getProject.rootExt[CGC3.Root].cgc3GCCArgs.split("\\s+").filter((_: String).nonEmpty)

	@TaskAction
	def assembleAction(): Unit = {
		getProject.ext[CGC3].yggdrasil.compile("obj")
	}
}
