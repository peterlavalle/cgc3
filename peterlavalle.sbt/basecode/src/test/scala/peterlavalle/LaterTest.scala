package peterlavalle

import junit.framework.TestCase
import org.junit.Assert._

class LaterTest extends TestCase {
  def testLaterNope(): Unit = {
    val laterSetOnce = new Later.SetOnce[Int]()
    val later: Later[Int] = laterSetOnce.later

    later ?? {
      case None =>
        ;
      case _ =>
        fail()
    }
  }

  def testLaterSome(): Unit = {
    val laterSetOnce = new Later.SetOnce[Int]()
    val later: Later[Int] = laterSetOnce.later

    laterSetOnce := 9

    later ?? {
      case Some(9) =>
        later ?? {
          case Some(redo) =>
            assertEquals(9, redo)
        }

    }
  }
}
