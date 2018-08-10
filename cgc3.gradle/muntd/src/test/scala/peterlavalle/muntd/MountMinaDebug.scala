package peterlavalle.muntd

import com.jcraft.jsch.Session

import scala.io.Source

object MountMinaDebug extends App {

	implicit class pAutoCloseable[C <: AutoCloseable](autoCloseable: C) {
		def trywith[O](action: C => O): O = {
			try {
				action apply autoCloseable
			} finally {
				autoCloseable.close()
			}
		}
	}

	lazy val mountMinaD =
		new MountMinaD("build" / "classes" EnsureExists)

	lazy val pipe: Int = mountMinaD.port

	// like; http://www.jcraft.com/jsch/examples/ScpTo.java.html
	// https://stackoverflow.com/questions/2003419/com-jcraft-jsch-jschexception-unknownhostkey#2003460
	// http://www.jcraft.com/jsch/examples/UserAuthPubKey.java.html

	lazy val remote =
		Source.fromFile("build" / "remote.txt").mkString.trim

	lazy val session: Session = {
		// select the SSH key
		(System.getProperty("user.home") / ".ssh/id_rsa")
			// use the SSH key to connect to a server
			.connectNonStrict(remote)
	}

	// pipe!
	session.setPortForwardingR(pipe, "localhost", pipe)

	println(s"opened $remote")


	// generate a random name
	val path = "`pwd`/" + RName(14)

	// actualize the thread
	mountMinaD.thread.trywith {
		_: AutoCloseable =>

			// always seems to cleanup on exit
			println(
				"TODO; " + List(
					s"mkdir $path",
					s"sshfs $path ${mountMinaD.user}@localhost:/ -odebug -p $pipe -oUserKnownHostsFile=/dev/null -oStrictHostKeyChecking=no -o uid=`id -u`,gid=`id -g`",
					s"rmdir $path"
				).reduce((_: String) + " && " + (_: String))
			)
			// MAC; can't look at directory


			println("type some rubbish and press 'enter' to exit")
			System.in.read()

	}
}
