package peterlavalle

import java.util

import org.gradle.api.{Project, Task}

import scala.reflect.ClassTag

object TaskBus {

	/**
		* connect to the group of tasks
		*/
	def apply(task: Task)(react: Task => Unit): Unit =
		apply[Task](task.getProject, task)(react)

	/**
		* connects something
		*/
	def apply[T <: Object](project: Project, data: T)(work: T => Unit)(implicit classTag: ClassTag[T]): Unit =
		synchronized {
			// show us off to the old ones
			anchor(project)[T].hook(data, work)
		}

	// lazily create the central anchor ... thing ...
	def anchor(project: Project): AnchorCollection =
		project.getRootProject.getExtensions.findByType(classOf[AnchorCollection]) match {
			case null =>
				project.getRootProject.getExtensions.create(classOf[AnchorCollection].getName, classOf[AnchorCollection])
			case anchor: AnchorCollection =>
				anchor
		}

	def connect[T <: Object](project: Project, data: T)(implicit classTag: ClassTag[T]): Unit =
		synchronized {
			anchor(project)[T].hook(data)
		}

	def consume[T <: Object](project: Project)(work: T => Unit)(implicit classTag: ClassTag[T]): Unit =
		synchronized {
			anchor(project)[T].hook(work)
		}

	final class AnchorExtension[T] {

		private val seen = new util.LinkedList[T]()
		private val todo = new util.LinkedList[T => Unit]()

		def hook(data: T): Unit = {

			// show us off to the old ones
			todo.foreach((_: T => Unit) apply data)

			// add us
			seen add data
		}

		def hook(data: T, work: T => Unit): Unit = {

			// show us off to the old ones
			todo.foreach((_: T => Unit) apply data)

			// show the old ones to us
			seen.foreach(work apply (_: T))

			// add both
			seen add data
			todo add work
		}

		def hook(work: T => Unit): Unit = {

			// show the old ones to us
			seen.foreach(work apply (_: T))

			// add us
			todo add work
		}
	}

	class AnchorCollection {
		private val anchor: Class[_] => AnchorExtension[_] = {
			val anchor = new util.HashMap[String, AnchorExtension[_]]()

			kind: Class[_] =>
				if (!anchor.containsKey(kind.getName))
					anchor.put(kind.getName, new AnchorExtension[Object]())
				anchor.get(kind.getName)
		}

		def apply[T](implicit classTag: ClassTag[T]): AnchorExtension[T] = {
			anchor(classTag.runtimeClass.asInstanceOf[Class[T]]).asInstanceOf[AnchorExtension[T]]
		}
	}
}
