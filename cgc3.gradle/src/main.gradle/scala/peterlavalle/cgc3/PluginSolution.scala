package peterlavalle.cgc3

import org.gradle.api.tasks.TaskAction
import org.gradle.api.{Project, Task}
import org.json.{JSONArray, JSONObject}
import peterlavalle._
import peterlavalle.cgc3.PluginSolution.TaskSolution

class PluginSolution extends APlugin {
	setup {
		project.defaultTasks("solution")
		require(project == project.getRootProject)
		project.createTask[TaskSolution]
	}
}

object PluginSolution extends PGradleProject {

	implicit class pLeaf(leaf: VCodeGradle.TVCTaskLeaf) {
		def taskLabel: String = s"${leaf.getProject.getPath} - ${leaf.getName}".trim
	}

	implicit class pRoot(leaf: VCodeGradle.TVCTaskRoot) {
		def taskLabel: String = leaf.getPath.split(":").last + " (root)"
	}

	def gradleTaskJSON(task: ATask, problemMatcher: Option[Object], args_ : Iterable[Object]): JSONObject = {

		var args: List[Object] = args_.toList

		if (!task.getProject.getRootProject.ext[CGC3.Root].daemon)
			args = "--no-daemon" :: args

		if (task.getProject.getRootProject.ext[CGC3.Root].verbose)
			args = "--stacktrace" :: args

		val jsonObject: JSONObject = {
			val jsonObject = new JSONObject()
			problemMatcher match {
				case None => jsonObject
				case Some(problemMatcher: String) => jsonObject.put("problemMatcher", problemMatcher)
				case Some(problemMatcher: JSONObject) => jsonObject.put("problemMatcher", problemMatcher)
			}
		}

		jsonObject
			.put("args", args.foldLeft(new JSONArray())((_: JSONArray) put (_: Object)))
			.put("command",
				osnamearch {
					case ("windows", _) => "${workspaceRoot}/gradlew.bat"
					case (_, _) => "./gradlew"
				}
			)
			.put("group", new JSONObject().put("kind", "build").put("isDefault", true))
			.put("options", new JSONObject().put("cwd", task.getProject.getRootDir.AbsolutePath))
			.put("type", "shell")
	}

	class TaskSolution extends ATask with VCodeGradle.TVCTaskRoot {

		TaskBus(this) {
			_: Task =>
				// just expose us; nothing to do here
				()
		}

		@TaskAction
		def solutionAction(): Unit = {

			def taskList: Iterable[JSONObject] = {
				def leafTasks: Stream[JSONObject] =
					VCodeGradle[VCodeGradle.TVCTaskLeaf, Project](
						getProject.getRootProject,
						(_: Project).getSubprojects,
						(_: Project).getTasks.toList
					).map {
						left: VCodeGradle.TVCTaskLeaf =>
							gradleTaskJSON(
								left,
								left.problemMatcher,
								left.taskArgs
							).put("label", left.taskLabel)
					}

				def rootTasks: Stream[JSONObject] =
					VCodeGradle[VCodeGradle.TVCTaskRoot, Project](
						getProject.getRootProject,
						(_: Project).getSubprojects,
						(_: Project).getTasks.toList
					).map {
						root: VCodeGradle.TVCTaskRoot =>
							gradleTaskJSON(
								root,
								None,
								List(root.getPath.split(":").last)
							).put("label", root.taskLabel)
					}

				(leafTasks ++ rootTasks)
					.sortBy((_: JSONObject).getString("label"))
			}

			new OverWriter(getProject.getProjectDir / ".vscode/tasks.json")
				.appund {
					new JSONObject()
						.put("version", "2.0.0")
						.put("tasks", taskList.distinctBy(_.getString("label")).foldLeft(new JSONArray())((_: JSONArray) put (_: JSONObject)))
						.toString(1)
				}
				.closeFile

			getProject.rootExt[CGC3.Root].ifVerbose {
				println("wrote .vscode/tasks.json")
			}

			new OverWriter(getProject.getProjectDir / ".vscode/launch.json")
				.appund {
					new JSONObject()
						.put("version", "0.2.0")
						.put("configurations",
							VCodeGradle[VCodeGradle.TLauncher, Project](
								getProject,
								(_: Project).getSubprojects,
								(_: Project).getTasks.toList
							).map {
								task: VCodeGradle.TLauncher =>

									val json = task.launchJSON

									// set the name
									require(!json.has("name"), "don't add this! I'll do it for you")
									json.put("name", task.asInstanceOf[Task].getProject.getPath)

									// set the default CWD
									if (!json.has("cwd"))
										json.put("cwd", getProject.getProjectDir.AbsolutePath)

									// add the pre-launch-task
									require(!json.has("preLaunchTask"), "don't add this! I'll do it for you")
									if (task.isInstanceOf[VCodeGradle.TVCTaskLeaf])
										json
											.put("preLaunchTask", task.asInstanceOf[VCodeGradle.TVCTaskLeaf].taskLabel)

									json
							}.foldLeft(new JSONArray())(_ put _)
						)
						.toString(1)
				}
				.closeFile

			getProject.rootExt[CGC3.Root].ifVerbose {
				println("wrote .vscode/launch.json")
			}
		}
	}

}
