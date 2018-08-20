package peterlavalle.cgc3

import org.gradle.api.Project

import scala.beans.BeanProperty
import scala.io.Source

object PluginTinFlu {

	class Extension {

		@BeanProperty
		var iterations: Int = 14

		@BeanProperty
		var blockSplittingMax: Int = 256
	}

	lazy val lines: Stream[String] =
		Source.fromInputStream(getClass.getResourceAsStream("PluginTinFlu.TIN.c"))
			.mkString
			.split("[\r \t]*\n")
			.toStream
}

class PluginTinFlu extends TPlugin with TProjectSetOnce {

	@deprecated(
		"pluck the setup{} et al from APlugin and push them to TPlugin"
	)
	override def getPath: String =
		project.getPath


	final override def apply(project: Project): Unit = {
		require(null != project.ext[APlugin.ACompilePlugin])

		this.project = project

		project.getRootProject.extend[PluginTinFlu.Extension]("tin")

		project.ext[CGC3].yggdrasil.run("tin", "cpp") {
			Tin.Flue(plonk)(
				project.rootExt[PluginTinFlu.Extension].iterations,
				project.rootExt[PluginTinFlu.Extension].blockSplittingMax
			)
		}
	}
}
