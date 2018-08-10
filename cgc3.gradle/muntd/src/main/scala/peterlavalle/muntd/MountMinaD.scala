package peterlavalle.muntd

import java.io.File
import java.security.PublicKey

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.session.ServerSession
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory

class MountMinaD(val root: File, val port: Int) {

	def this(root: File) =
		this(
			root,
			6000 + (1000 * Math.random()).toInt
		)

	lazy val user = RName(14)

	private var key: Option[PublicKey] = None

	// dialHomeDaemon!
	lazy val dialHomeDaemon: SshServer = {
		val dialHomeDaemon: SshServer = SshServer.setUpDefaultServer()

		// hmm ... unlikely
		dialHomeDaemon.setPort(port)

		// ?
		dialHomeDaemon.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File(System.getProperty("user.home")) / s"${getClass.getSimpleName}.ser"))

		dialHomeDaemon.setPublickeyAuthenticator {
			(username: String, key: PublicKey, _: ServerSession) =>
				if (this.key.isEmpty && username == user)
					this.key = Some(key)
				username == user && this.key.get.toString == key.toString
		}

		// setup our thing; need an sftp-subsystem-factory and a VFS
		dialHomeDaemon.setFileSystemFactory(new VirtualFileSystemFactory(root.toPath))
		dialHomeDaemon.setSubsystemFactories {
			List {
				new SftpSubsystemFactory.Builder()
					//			.withFileSystemAccessor {
					//				new SftpFileSystemAccessor {
					//				}
					//			}
					.build()
			}
		}

		dialHomeDaemon
	}

	def thread: AutoCloseable = {
		val thread: Thread with AutoCloseable = new Thread() with AutoCloseable {
			override def run(): Unit =
				synchronized {
					notifyAll()
					dialHomeDaemon.start()
					wait()
				}

			override def start(): Unit = {
				synchronized {
					super.start()
					wait()
				}
			}

			override def close(): Unit =
				synchronized {
					notify()
					join()
				}
		}
		thread start()
		thread
	}


}
