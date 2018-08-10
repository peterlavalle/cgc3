package peterlavalle.frp

import junit.framework.TestCase
import org.easymock.EasyMock
import org.junit.Assert._

class SignalTest extends TestCase {
  def testConstant(): Unit = {

    val signal: Signal[Unit, Int] = Signal.constant(3)

    assertEquals(
      3,
      (signal ! Stream((), (), (), ())).last
    )
    assertEquals(
      3,
      (signal ! Stream((), (), (), (), (), (), (), ())).last
    )
    assertEquals(
      3,
      (signal ! Stream(())).last
    )
  }

  def testCounter(): Unit = {

    val signal: Signal[Unit, Int] = Signal.reusing(0)((_: Unit, v: Int) => v + 1)

    assertEquals(
      4,
      (signal ! Stream((), (), (), ())).last
    )
    assertEquals(
      8,
      (signal ! Stream((), (), (), (), (), (), (), ())).last
    )
    assertEquals(
      1,
      (signal ! Stream(())).last
    )
  }

  def testCycle(): Unit = {

    val signal: Signal[Unit, String] =
      Signal.cycle(List("a", "b"))

    assertEquals(
      List("a", "b", "a", "b", "a", "b"),
      signal ! Stream((), (), (), (), (), ())
    )
  }

  def testCacheSignal(): Unit = {
    val mockFunction: String => Int = {
      val mockFunction: String => Int = EasyMock.createMock(classOf[String => Int])

      EasyMock.expect(mockFunction("one")).andReturn(3).once()
      EasyMock.expect(mockFunction("seven")).andReturn(12).once()

      EasyMock.replay(mockFunction)

      mockFunction
    }
    val value: Signal[String, Int] =
      Signal.stateless(mockFunction)
        .cache(2)

    assertEquals(
      Stream(3, 3, 3, 12, 3, 3, 3, 3, 3, 3),
      value ! Stream("one", "one", "one", "seven", "one", "one", "one", "one", "one", "one")
    )

    EasyMock.verify(mockFunction)
  }

  def testCacheFunction(): Unit = {
    val mockFunction: String => Int = {
      val mockFunction: String => Int = EasyMock.createMock(classOf[String => Int])

      EasyMock.expect(mockFunction("one")).andReturn(3).once()
      EasyMock.expect(mockFunction("seven")).andReturn(12).once()

      EasyMock.replay(mockFunction)

      mockFunction
    }
    val value: Signal[String, Int] =
      Signal.cache(2, mockFunction)

    assertEquals(
      Stream(3, 3, 3, 12, 3, 3, 3, 3, 3, 3),
      value ! Stream("one", "one", "one", "seven", "one", "one", "one", "one", "one", "one")
    )

    EasyMock.verify(mockFunction)
  }
}
