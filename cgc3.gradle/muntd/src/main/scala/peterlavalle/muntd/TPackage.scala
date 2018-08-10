package peterlavalle.muntd

import java.io.{File, InputStream}

import com.jcraft.jsch.{Channel, ChannelExec, JSch, Session}

import scala.util.matching.Regex

trait TPackage {

	import TPackage._

	implicit class SSHKey(file: File) {

		// https://stackoverflow.com/questions/2003419/com-jcraft-jsch-jschexception-unknownhostkey#2003460
		// http://www.jcraft.com/jsch/examples/UserAuthPubKey.java.html

		def connectNonStrict(session: String): Session = {

			session match {
				case rUserHostPort(user, host, port) =>
					connectNonStrict(
						user,
						host,
						port.toInt
					)
			}
		}

		def connectNonStrict(user: String, host: String, port: Int): Session = {
			val session: Session = {
				val sch = new JSch()

				// authenticate with our private key http://www.jcraft.com/jsch/examples/UserAuthPubKey.java.html
				sch.addIdentity(file.AbsolutePath)

				sch.getSession(user, host, port)
			}

			// don't care about known hosts https://stackoverflow.com/questions/2003419/com-jcraft-jsch-jschexception-unknownhostkey#2003460
			// ... in general; this is a bad idea and it allows MITM attacks
			session.setConfig("StrictHostKeyChecking", "no")

			session.connect()

			session

		}
	}

	implicit class pSSHExec(session: Session) {

		def exec[O](command: String, head: O)(action: (O, TEvent) => O): O = {

			val channel: ChannelExec = session.openChannel("exec").asInstanceOf[ChannelExec]
			channel.setCommand(command)

			channel.setInputStream(null)

			val closed: Boolean = channel.isClosed

			channel.connect()

			val out: InputStream = channel.getInputStream
			val err: InputStream = channel.getErrStream

			def loop(last: O): O = {
				out.read() match {
					case o: Int if -1 != o =>
						loop(
							action(last, EventOut(o))
						)

					case _ =>
						err.read() match {
							case e: Int if -1 != e =>
								last

							case _ =>
								if (channel.isClosed)
									action(last, Done(channel.getExitStatus))
								else {
									Thread.sleep(10)
									loop(last)
								}
						}
				}
			}

			loop(head)
		}

		def exec(command: String)(action: TEvent => Unit): Int =
			exec(command, None.asInstanceOf[Option[Int]]) {
				case (Some(_), _) =>
					sys.error("Contrarily; this should not have happened")
				case (None, d@Done(r)) =>
					action(d)
					Some(r)
				case (None, e) =>
					action(e)
					None
			} match {
				case None =>
					sys.error("result code was not found")

				case Some(r: Int) => r
			}
	}

	implicit class pChannel(channel: Channel) {
		def isOpen: Boolean = !channel.isEOF
	}

}

case object TPackage extends TPackage {

	sealed trait TEvent

	case class EventOut(o: Int) extends TEvent

	case class Done(r: Int) extends TEvent with TLine {
		override def line: String = s"r = $r"
	}

	sealed trait TLine {
		def line: String
	}

	case class LineOut(line: String) extends TLine

	case class LineErr(line: String) extends TLine

	val rUserHostPort: Regex = "(.+)@(.+):(\\d+)".r

}
