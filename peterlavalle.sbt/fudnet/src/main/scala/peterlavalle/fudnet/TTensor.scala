package peterlavalle.fudnet

import peterlavalle.fudnet.TTensor.TKind

import scala.reflect.ClassTag

trait TTensor {
	// can this be part of the signature? ... no ... no I can't have numbers in the type signatures right?
	def kind: TKind
}

object TTensor {

	sealed trait TKind

	sealed trait TKindScalar extends TKind

	case object KindDouble extends TKindScalar

	case object KindSingle extends TKindScalar

	case object KindHalf16 extends TKindScalar

	case class KindVector[K <: TKindScalar](i: Int)(implicit val sTag: ClassTag[K]) extends TKind

	def constant(value: Double): TTensor =
		new TTensor {
			override val kind: TKind = KindDouble
		}

	def constant(head: Double, neck: Double, tail: Double*): TTensor =
		new TTensor {
			override val kind: TKind =
				KindVector[KindDouble.type](tail.length + 2)
		}

	def trained[T <: TKindScalar](s: Int)(implicit tTag: ClassTag[T]): TTensor =
		new TTensor {
			override val kind: TKind =
				KindVector[T](s)
		}

	def input[T <: TKindScalar](s: Int)(implicit tTag: ClassTag[T]): TTensor =
		new TTensor {
			override val kind: TKind =
				KindVector[T](s)
		}


}
