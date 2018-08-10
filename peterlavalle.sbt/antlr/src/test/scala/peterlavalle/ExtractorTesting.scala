package peterlavalle

/**
	* mess around with using strings and tokens for matching
	*/
object ExtractorTesting extends App {


	//	"https://stackoverflow.com/questions/16098066/how-to-use-extractor-in-polymorphic-unapply" halt


	def useCase[T](box: Box[T]) =
		println(box match {
			case StringBox("StringBoxxx") => "I found the StringBox!"
			case StringBox(s) => "Some other StringBox"
			case BooleanBox(b) => {
				if (b) "Omg! its true BooleanBox !"
				else "its false BooleanBox :("
			}
			case DoubleBox(x) => {
				if (x > 3.14) "DoubleBox greater than pie !"
				else if (x == 3.14) "DoubleBox with a pie !"
				else "DoubleBox less than a pie !"
			}
			case _ => "What is it yaa ?"
		})

	def doop(box: Box[_]): Unit =
		???

	abstract class Box[T](val v: T)

	class StringBox(sValue: String) extends Box(sValue)

	class BooleanBox(sValue: Boolean) extends Box(sValue)

	class DoubleBox(sValue: Double) extends Box(sValue)

	object Box {
		def apply(s: String) = new StringBox(s)

		def apply(b: Boolean) = new BooleanBox(b)

		def apply(d: Double) = new DoubleBox(d)

	}

	object StringBox {
		def unapply(b: StringBox) = Some(b.v)
	}

	object BooleanBox {
		def unapply(b: BooleanBox) = Some(b.v)
	}

	useCase(Box("StringBoxxx")) //> res0: String = I found the StringBox!
	useCase(Box("Whatever !")) //> res1: String = Some other StringBox
	useCase(Box(true)) //> res2: String = Omg! its true BooleanBox !
	useCase(Box(false)) //> res3: String = its false BooleanBox :(
	useCase(Box(4)) //> res4: String = DoubleBox greater than pie !
	useCase(Box(3.14)) //> res5: String = DoubleBox with a pie !
	useCase(Box(2)) //> res6: String = DoubleBox less than a pie !


	//
	// peter's idea

	object DoubleBox {
		def unapply(b: DoubleBox) = Some(b.v)
	}

	//	Box(3.14) match {
	//		case Box(3.14) =>
	//			???
	//	}

	sys.error("run/com[are two instancse oif the thing in the matcher")
}
