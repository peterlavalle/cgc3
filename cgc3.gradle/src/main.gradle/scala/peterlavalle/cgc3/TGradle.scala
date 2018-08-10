package peterlavalle.cgc3

import java.io.File

import org.gradle.api.internal.AbstractTask
import org.gradle.api.{Project, UnknownDomainObjectException}

import scala.reflect.ClassTag

trait TGradle {

	implicit class pProject(project: Project) {

		/**
			* reads a target-specific dir in build
			*/
		def tripletDir: File = project.getBuildDir / ('_' + System.getProperty("os.name") + '_').replaceAll("([^\\w]|_)+", "_").tail.reverse.tail.reverse / System.getProperty("os.arch")

		def rootExt[E <: AnyRef](implicit eTag: ClassTag[E]): E =
			project.getRootProject.ext[E]

		/**
			* find an extension by (implicit) type
			*/
		def ext[E <: AnyRef](implicit eTag: ClassTag[E]): E =
			try {
				project.getExtensions.getByType(eTag.runtimeClass.asInstanceOf[Class[E]])
			} catch {
				case _: UnknownDomainObjectException =>
					null.asInstanceOf[E]
			}

		def extend[E <: AnyRef](name: String)(implicit eTag: ClassTag[E]): E =
			try {
				project.getExtensions.getByType(eTag.runtimeClass.asInstanceOf[Class[E]])
			} catch {
				case _: UnknownDomainObjectException =>
					project.getExtensions.create(
						eTag.runtimeClass.getName,
						eTag.runtimeClass.asInstanceOf[Class[E]]
					)
			}

		def /(path: String): File =
			if ('/' == path.head)
				project.getRootProject / path.tail
			else if (':' == path.head)
				project.getBuildDir / System.getProperty("os.name") / System.getProperty("os.arch")
			else
				project.getProjectDir / path

		def createTask[T <: AbstractTask](implicit tTag: ClassTag[T]): T = {
			val taskClass: Class[T] = tTag.runtimeClass.asInstanceOf[Class[T]]

			require(
				project.getTasks.filterTo[T].isEmpty
			)

			project.getTasks.create(
				taskClass.getSimpleName.replace("Task", "").toLowerCase(),
				taskClass
			)
		}
	}

}

object TGradle extends TGradle {

	trait TTask {
		def setup(project: Project)
	}

}
