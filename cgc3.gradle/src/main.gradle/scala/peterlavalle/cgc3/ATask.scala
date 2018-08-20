package peterlavalle.cgc3

import org.gradle.api.DefaultTask

abstract class ATask extends DefaultTask with TGradle {
	setDescription {
		getClass
			.getName
			.replaceAll("_Decorated$", "")
			.replaceAll("_decorated$", "")
	}
	setGroup("cgc3")

}
