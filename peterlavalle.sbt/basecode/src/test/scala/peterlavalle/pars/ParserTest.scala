package peterlavalle.pars

import junit.framework.TestCase
import org.junit.Assert._

class ParserTest extends TestCase {
  def testWhat() =
    assertEquals(
      Parser.Parsed(1, "abc".toStream),
      Parser.Core.Return[Char, Int](1)("abc".toStream)
    )
}
