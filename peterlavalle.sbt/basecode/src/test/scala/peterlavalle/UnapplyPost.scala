package peterlavalle

object UnapplyPost extends App {

  val d = Varp(3)
  val q = Varp(4)

  (0 until 9).foreach {
    case d(num) => println("> foo;> " + num)

    case q(num) => println("> bar;> " + num)

    //unapply is invoked
    case x => println(s"i cannot calculate $x")
  }


  case class Varp(i: Int) {
    def unapply(z: Int): Option[String] =
      z % i match {
        case 0 => Some("goodie")
        case 2 => Some("okee")
        case _ => None
      }
  }

}
