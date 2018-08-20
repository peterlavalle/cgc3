package peterlavalle.cgc3

import org.gradle.api.Project
import peterlavalle.{TaskBus, cgc3}

abstract class APlugin extends TPlugin with TProjectSetOnce {

	override final def getPath: String = project.getPath

	final override def apply(target: Project): Unit = {

		this.project = target

		// check that we're the only plugin
		require(
			null == target.ext[CGC3] && null == target.ext[APlugin]
		)

		// add the main extension
		// ... umm ... do something if we're solution since we shouldn't have a yggdrasil
		val extension =
		if (isInstanceOf[PluginSolution])
			new cgc3.CGC3.Root(target, (path: String) =>
				target.tripletDir / path
			)
		else
			new cgc3.CGC3(target, (path: String) =>
				target.tripletDir / path
			)
		target.getExtensions.add("cgc3", extension)
		TaskBus.connect[CGC3](target, extension)


		// add the plugin
		target.getExtensions.add("APlugin", this)

		// do the real work
		actualSetup match {
			case Some(action) =>
				actualSetup = null
				action()
		}
	}

	def setup(action: => Unit): Unit = {
		actualSetup match {
			case null =>
				failure("The task has already been run")
			case None =>
				actualSetup =
					Some {
						() => action
					}
		}
	}

	private var actualSetup: Option[() => Unit] = None
}

object APlugin {

	abstract class ACompilePlugin extends APlugin {

	}

}
