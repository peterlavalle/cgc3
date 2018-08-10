package peterlavalle

import scala.language.postfixOps


trait PFunction {

  implicit class PFunction[I](self: I => Unit) {
    def <<(next: I => Unit): I => Unit = {
      i: I => {
        self(i)
        next(i)
      }
    }

    def >>(left: I => Unit): I => Unit = {
      i: I => {
        left(i)
        self(i)
      }
    }
  }

  implicit class pProcedure3[A0, A1, A2](self: (A0, A1, A2) => Unit) {
    def <<(next: (A0, A1, A2) => Unit): (A0, A1, A2) => Unit = {
      (a0: A0, a1: A1, a2: A2) =>
        self(a0, a1, a2)
        next(a0, a1, a2)
    }
  }

}
