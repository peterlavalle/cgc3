package peterlavalle.cgc3

import java.io.File

import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

class TaskCheck extends ATask
	with VCodeGradle.TVCTaskGCC
	with VCodeGradle.TVCTaskRoot
	with TCGCProblemMatcher {

	lazy val assemble: TaskAssemble =
		getProject.getTasks.find((_: Task).isInstanceOf[TaskAssemble]) match {
			case Some(assemble: TaskAssemble) =>
				assemble
		}

	@TaskAction
	def checkAction(): Unit = {
		val dir: File = getProject.ext[CGC3].yggdrasil.compile("obj")
		dir.list().toList match {

			case Nil | null =>
				failure("No outputs came from task")
			case list: List[String] =>

				val exe: String = assemble.target.getName

				require(
					list.contains(exe),
					s"Couldn't find executable `$exe`"
				)
				assume(getProject.getRootDir == getProject.getRootProject.getRootDir)
				assume(getProject.getRootDir == getProject.getRootProject.getProjectDir)

				// invoke from the root dir
				getProject.getRootDir.Shell {
					getProject.rootExt[CGC3.Root].ifVerbose {
						System.err.println("TODO; fixme; getProject.getRootProject.getRootDir.PathTo(assemble.target)")
					}

					assemble.target.makeExecutable
				}
					.invoke(
						(o: String) => println(o),
						(e: String) => errorln(e)
					) match {
					case 0 =>
						getProject.rootExt[CGC3.Root].ifVerbose {
							println("tests passed")
						}
					case r =>
						failure(s"tests returned r = $r")
				}
		}
	}
}

