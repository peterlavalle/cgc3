package peterlavalle.pars

import scala.collection.immutable.Stream.Empty

trait Parser[C, A] {
  def apply(input: Stream[C]): Parser.TResult[C, A]

  final def apply(input: Iterable[C]): Parser.TResult[C, A] = this (input.toStream)

  def >>=[B](next: A => Parser[C, B]): Parser[C, B] =
    (inp: Stream[C]) =>
      apply(inp) match {
        case _: Parser.Failed[C, A] =>
          Parser.Failed()
        case Parser.Parsed(data, tail) =>
          next(data)(tail)
      }
}

object Parser {

  sealed trait TResult[C, A]

  case class Parsed[C, A](data: A, tail: Stream[C]) extends TResult[C, A]

  case class Failed[C, A]() extends TResult[C, A]

  case object Core {
    def Return[C, A](data: A): Parser[C, A] =
      (inp: Stream[C]) =>
        Parsed(data, inp)

    def Failure[C, A](data: A): Parser[C, A] =
      (inp: Stream[C]) =>
        Failed()

    def Item[C]: Parser[C, C] = {
      case Empty => Failed()
      case steam: Stream[C] =>
        val head: C = steam.head
        val tail: Stream[C] = steam.tail
        Parsed[C, C](head, tail)
    }

  }

}
