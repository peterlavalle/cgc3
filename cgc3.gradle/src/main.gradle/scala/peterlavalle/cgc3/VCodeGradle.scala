package peterlavalle.cgc3

import java.io.File

import org.gradle.api.Task
import org.json.{JSONArray, JSONObject}
import peterlavalle.OverWriter

import scala.reflect.ClassTag

sealed trait VCodeGradle

object VCodeGradle {

	def apply[T <: VCodeGradle, O](root: O, expand: O => Iterable[O], unpack: O => Iterable[_ <: Object])(implicit tTag: ClassTag[T], oTag: ClassTag[O]): Stream[T] = {
		// start with the root
		Stream(root)

			// expand the stream
			.expand(expand)

			// keep only distinct entries
			.distinct

			// unpack
			.flatMap((p: O) => unpack(p))

			// filter
			.filterTo[T].toStream
	}

	sealed trait TVCTask extends ATask with VCodeGradle

	/**
		* the task creates something in launch.json
		*/
	trait TLauncher extends VCodeGradle {
		def launchJSON: JSONObject
	}

	/**
		* this task does 95% of the TDebug task's logic
		*/
	trait TGDBLauncher extends TLauncher {
		this: Task =>
		assume(isInstanceOf[TVCTaskLeaf])

		def target: File

		def launchJSON: JSONObject =
			new JSONObject()
				.put("type", "cppdbg")
				.put("request", "launch")
				.put("cwd", getProject.getRootDir.AbsolutePath)
				.put("args", new JSONArray())
				.put("stopAtEntry", false)
				.put("environment", new JSONArray())
				.put("externalConsole", true)
				.put("MIMode", "gdb")
				.put("program", target.AbsolutePath)
				.put("miDebuggerPath", gdbPath.AbsolutePath)
				.put("setupCommands",
					{
						new JSONArray().put {
							new JSONObject()
								.put("description", "Enable pretty-printing for gdb")
								.put("text", "-enable-pretty-printing")
								.put("ignoreFailures", true)
						}
					})

		def gdbPath: File =
			osnamearch {
				case ("windows", _) =>
					new OverWriter(getProject.getRootProject.getBuildDir / "pGDB.bat")
						.appund("@ECHO OFF\n\n")
						.appund(":this bat file gives us an absolute path to the GDB command without requiring us to know where that exe is\n\n")
						.appund("gdb %*\n\n")
						.closeFile

				case ("mac", "x86_64") =>
					System.err.println("WARN; Debugging on Mac is ... not tested ... yet")
					new OverWriter(getProject.getRootProject.getBuildDir / "pLLDB.sh")
						.appund("#!/bin/bash\n\n")
						.appund("#this bash file gives us an absolute path to the LLDB command without requiring us to know where it is\n\n")
						.appund("lldb \"$@\"\n\n")
						.closeFile
						.makeExecutable

				case ("linux", "amd64") =>
					System.err.println("WARN; Debugging on Linux is ... not tested ... yet")
					new OverWriter(getProject.getRootProject.getBuildDir / "pGDB.sh")
						.appund("#!/bin/bash\n\n")
						.appund("#this bash file gives us an absolute path to the GDB command without requiring us to know where it is\n\n")
						.appund("gdb \"$@\"\n\n")
						.closeFile
						.makeExecutable
			}
	}

	trait TVCTaskRoot extends TVCTask

	/**
		* the task creates something in build.json
		*/
	trait TVCTaskLeaf extends TVCTask {
		def problemMatcher: Option[Object]

		def taskArgs: List[Object] = List(getPath)
	}

	trait TVCTaskGCC extends TVCTaskLeaf {
		this: Task =>
		override def taskArgs: List[Object] =
			super.taskArgs.reverse match {
				case head :: tail =>
					(head :: s"-Dcgc3GCCArgs='${getProject.getRootProject.ext[CGC3.Root].cgc3GCCArgs}'" :: tail)
						.reverse
			}
	}

}
