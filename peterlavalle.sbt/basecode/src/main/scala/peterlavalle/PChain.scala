package peterlavalle

import peterlavalle.Sugar.STilde

trait PChain {

	implicit class pChain[C](c: C) {
		def ~[P](seq: Iterable[P]): STilde[(C, P) => C, C] =
			(fold: (C, P) => C) =>
				seq.foldLeft(c)(fold)
	}

}
