package peterlavalle.swung

import java.awt.event._
import java.awt.{Container, Frame}
import javax.swing.{JDialog, JTextArea, _}

trait TPackage extends TMenus {

	def osName[T](action: String => T): T =
		action(
			System.getProperty("os.name").toLowerCase().split("[^\\w]").head
		)

	def errorDialog(parent: => Frame)(message: String => String): (String => Unit) =
		(fail: String) =>
			new JDialog(parent)
				.getContentPane
				.addComponent(
					new JTextArea()
						.putEditable(false)
						.putln(message(fail))
				)
				.setVisible(true)


	implicit class WrappedJFrame2[F <: JFrame](frame: F) {

		def dispatchEvent(id: Int): Unit =
			frame.dispatchEvent(new WindowEvent(frame, id))

		def onClosed(doit: => Unit): F = {
			frame.addWindowListener(
				new WindowListener {
					override def windowDeactivated(e: WindowEvent): Unit = {}

					override def windowOpened(e: WindowEvent): Unit = {}

					override def windowIconified(e: WindowEvent): Unit = {}

					override def windowClosed(e: WindowEvent): Unit = doit

					override def windowActivated(e: WindowEvent): Unit = {}

					override def windowClosing(e: WindowEvent): Unit = {}

					override def windowDeiconified(e: WindowEvent): Unit = {}
				}
			)
			frame
		}

		def onClosing(doit: => Unit): F = {
			frame.addWindowListener(
				new WindowListener {
					override def windowDeactivated(e: WindowEvent): Unit = {}

					override def windowOpened(e: WindowEvent): Unit = {}

					override def windowIconified(e: WindowEvent): Unit = {}

					override def windowClosing(e: WindowEvent): Unit = doit

					override def windowActivated(e: WindowEvent): Unit = {}

					override def windowClosed(e: WindowEvent): Unit = {}

					override def windowDeiconified(e: WindowEvent): Unit = {}
				}
			)
			frame
		}

		def onOpened(doit: => Unit): F = {
			frame.addWindowListener(
				new WindowListener {
					override def windowDeactivated(e: WindowEvent): Unit = {}

					override def windowClosing(e: WindowEvent): Unit = {}

					override def windowIconified(e: WindowEvent): Unit = {}

					override def windowOpened(e: WindowEvent): Unit = doit

					override def windowActivated(e: WindowEvent): Unit = {}

					override def windowClosed(e: WindowEvent): Unit = {}

					override def windowDeiconified(e: WindowEvent): Unit = {}
				}
			)
			frame
		}

		def onMoved(doit: => Unit): F = {
			frame.addComponentListener(
				new ComponentListener {
					override def componentHidden(e: ComponentEvent): Unit = {}

					override def componentShown(e: ComponentEvent): Unit = {}

					override def componentResized(e: ComponentEvent): Unit = {}

					override def componentMoved(e: ComponentEvent): Unit = doit
				}
			)
			frame
		}

		def onSized(doit: => Unit): F = {
			frame.addComponentListener(
				new ComponentListener {
					override def componentHidden(e: ComponentEvent): Unit = {}

					override def componentShown(e: ComponentEvent): Unit = {}

					override def componentResized(e: ComponentEvent): Unit = doit

					override def componentMoved(e: ComponentEvent): Unit = {}
				}
			)
			frame
		}
	}

	implicit class WrappedJTextArea[J <: JTextArea](textArea: J) {
		def putln(text: String): J = {
			//	SwingNow {
			textArea.append(text)
			textArea.append("\n")
			//	}
			textArea
		}

		def logln(prefix: String): String => Unit =
			(line: String) =>
				SwingNow {
					textArea.append(prefix + line + "\n")
				}

		def putEditable(value: Boolean): J = {
			textArea.setEditable(value)
			textArea
		}
	}

	def SwingNot(thing: => Unit): Unit =
		if (!SwingUtilities.isEventDispatchThread)
			thing
		else
			new Thread {
				override def run(): Unit = {
					thing
				}
			}.start()

	def SwingNow(thing: => Unit): Unit =
		if (SwingUtilities.isEventDispatchThread)
			thing
		else
			SwingUtilities.invokeAndWait(
				new Runnable {
					override def run(): Unit = {
						thing
					}
				}
			)

	def SwingVal[T](thing: => T): T = {
		lazy val solved: T = thing

		if (SwingUtilities.isEventDispatchThread)
			solved
		else {
			SwingUtilities.invokeAndWait(
				new Runnable {
					override def run(): Unit = {
						solved
					}
				}
			)
			solved
		}
	}

	implicit class WrappedJComponent[J <: JComponent](component: J) {
		def putEnabled(enabled: Boolean): J = {
			component.setEnabled(enabled)
			component
		}
	}

	implicit class WrappedContainer[J <: Container](component: J) {
		def addComponent(other: JComponent): J = {
			component add other
			component
		}
	}

	implicit class WrappedAbstractButton[B <: AbstractButton](abstractButton: B) {
		def onAction(listener: => Unit): B = {
			abstractButton.addActionListener(
				new ActionListener {
					override def actionPerformed(e: ActionEvent): Unit =
						listener
				}
			)
			abstractButton
		}
	}

}
