package peterlavalle.fudnet

import scala.reflect.ClassTag

abstract class AFudLet[O](implicit final val oTag: ClassTag[O]) {
	def input[I](name: String)(implicit iTag: ClassTag[I]): TNode[I] =
		sys.error(???)

	def output: TNode[O]
}
