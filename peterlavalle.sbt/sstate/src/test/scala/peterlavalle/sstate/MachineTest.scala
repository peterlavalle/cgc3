package peterlavalle.sstate

import junit.framework.TestCase
import org.junit.Assert._

class MachineTest extends TestCase {
	def testThrough(): Unit = {

		val fsm: Machine.Shell[Int, Machine.TState, Machine.TEvent] =
		// start a new FSM
			Machine[Int, Machine.TState, Machine.TEvent]()

				// we can be locked
				.addState(MachineTest.Locked)

				// we can be unlocked
				.addState(MachineTest.UnLocked)

				// when we're LOCKED, if COIN then become UNLOCKED and modify our ghost
				.onEvent(MachineTest.Locked, MachineTest.Coin, MachineTest.UnLocked)(_ + 1)

				// when we're UNLOCKED, if PUSH, become LOCKED and don't do anything
				.transition(MachineTest.UnLocked, MachineTest.Push, MachineTest.Locked)

				// create a machine
				.apply(MachineTest.Locked, 0)

		//
		// cool, so when a coin is applied - the ghost is incremented
		//
		// so let's do a bunch of tests to hit all those stubs!
		//

		assertEquals(0, fsm.ghost)

		assertEquals(0, (fsm ! MachineTest.Push).ghost)
		assertEquals(0, fsm.ghost)

		assertEquals(1, (fsm ! MachineTest.Coin).ghost)
		assertEquals(0, fsm.ghost)

		assertEquals(0, (fsm ! MachineTest.Push ! MachineTest.Push).ghost)
		assertEquals(0, fsm.ghost)

		assertEquals(1, (fsm ! MachineTest.Push ! MachineTest.Coin).ghost)
		assertEquals(0, fsm.ghost)

		assertEquals(1, (fsm ! MachineTest.Coin ! MachineTest.Push).ghost)
		assertEquals(0, fsm.ghost)

		assertEquals(1, (fsm ! MachineTest.Coin ! MachineTest.Coin).ghost)
		assertEquals(0, fsm.ghost)

		assertEquals(0, (fsm ! MachineTest.Push ! MachineTest.Push ! MachineTest.Push).ghost)
		assertEquals(2, (fsm ! MachineTest.Coin ! MachineTest.Push ! MachineTest.Coin).ghost)
		assertEquals(1, (fsm ! MachineTest.Coin ! MachineTest.Coin ! MachineTest.Coin).ghost)


		assertEquals(0, fsm.ghost)

		fsm match {
			case Machine(s, g) =>
				assertEquals(MachineTest.Locked, s)
				assertEquals(0, g)
		}

		fsm ! MachineTest.Push match {
			case Machine(s, g) =>
				assertEquals(MachineTest.Locked, s)
				assertEquals(0, g)
		}

		fsm ! MachineTest.Coin match {
			case Machine(s, g) =>
				assertEquals(MachineTest.UnLocked, s)
				assertEquals(1, g)
		}
	}
}

object MachineTest {

	case object Locked extends Machine.TState

	case object UnLocked extends Machine.TState

	case object Coin extends Machine.TEvent

	case object Push extends Machine.TEvent


}
