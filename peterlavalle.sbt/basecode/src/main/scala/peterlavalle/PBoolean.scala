package peterlavalle

import peterlavalle.Sugar.LElse

trait PBoolean {

	implicit class lBoolean(b: => Boolean) {
		def ?/[A](iT: => A): LElse[A, A] =
			new LElse[A, A] {
				override def :/(iF: => A): A =
					if (b) iT else iF
			}
	}

}
