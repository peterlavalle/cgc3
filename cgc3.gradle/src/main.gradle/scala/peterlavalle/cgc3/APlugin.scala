package peterlavalle.cgc3

import org.gradle.api.Project
import peterlavalle.{TaskBus, cgc3}

abstract class APlugin extends TPlugin {
	final override def apply(target: Project): Unit = {

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
		setup(target)
	}

	def setup(project: Project)
}

object APlugin {

	abstract class ACompilePlugin extends APlugin {

	}

}
