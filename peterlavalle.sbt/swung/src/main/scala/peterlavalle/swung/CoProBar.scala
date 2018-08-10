package peterlavalle.swung

import javax.swing.{BoxLayout, JPanel, JProgressBar, SwingConstants}

object CoProBar extends TPackage {

	type LabelBack = String => Unit
	type ToDo = LabelBack => Unit

	type Step = () => Unit
	type TextOut = String => Unit
	type SequenceSource = TextOut => List[Step]

	class Cluster extends JPanel {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

		def attach(label: String = null)(sequenceSource: CoProBar.SequenceSource): Cluster = {
			SwingNow {
				add(new CoProBar(label)(sequenceSource))
			}
			this
		}

		def activate(labelBack: LabelBack)(done: => Unit): Unit = {

			def recu(todo: List[CoProBar]): Unit =
				todo match {
					case Nil =>
						done

					case head :: tail =>
						head.activate(labelBack) {
							recu(tail)
						}
				}

			recu(
				getComponents.toList.map(_.asInstanceOf[CoProBar])
			)
		}
	}

}

class CoProBar(label: String)(sequenceSource: CoProBar.SequenceSource) extends JProgressBar(SwingConstants.HORIZONTAL) with TPackage {

	import CoProBar._

	setString(if (null != label) s"$label; <waiting>" else "")
	setStringPainted(true)

	def activate(labelBack: LabelBack)(done: => Unit) =
		SwingNow {
			SwingNot {
				val sequence: List[CoProBar.Step] = sequenceSource(s => labelBack(label + "; " + s))
				val length: Int = sequence.length
				val numerator: Double = 100.0 / length

				setMinimum(0)
				setMaximum(length)
				setStringPainted(true)

				sequence.toStream.zipWithIndex.map { case (s, i) => (s, 1 + i) }.foreach {
					case (r: CoProBar.Step, i: Int) =>

						val message =
							(if (null != label) label + "; " else "") + s"${(numerator * (i)).toInt}%"

						SwingNow {
							setValue(i)
							setString(message)
						}

						r.apply()
				}

				done
			}
		}
}
