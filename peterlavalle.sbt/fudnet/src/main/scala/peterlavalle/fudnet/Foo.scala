package peterlavalle.fudnet

object Foo extends AFudLet[Float] with App {

	override def output: TNode[Float] =
		input[Float]("read")
}
