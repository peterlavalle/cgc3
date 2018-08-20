package peterlavalle.cgc3

trait Plonk {
	def ifVerbose(action: => Unit): Unit

	def outline(o: => String): Unit

	def errline(e: => String): Unit
}

