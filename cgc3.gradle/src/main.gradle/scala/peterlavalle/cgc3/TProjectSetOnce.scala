package peterlavalle.cgc3

import org.gradle.api.{GradleException, Project}

trait TProjectSetOnce {

	private var _project: Option[Project] = None

	def project: Project = this._project.get

	def project_=(project: Project): Unit = {
		_project match {
			case None =>
				_project = Some(project)
			case Some(_) =>
				throw new GradleException(s"Project has already been set ong ${getClass.getName}")
		}
	}
}
