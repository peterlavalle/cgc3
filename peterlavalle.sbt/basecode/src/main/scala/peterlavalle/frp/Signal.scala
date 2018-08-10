package peterlavalle.frp

import peterlavalle.frp.Signal._

import scala.collection.immutable.Stream.Empty
import scala.language.implicitConversions

/**
  * This class is the "plumbing" of this system. a signal is a thing that can return some value, and, returns a new singal when it does that.
  *
  * like a function with state
  *
  * ... because it's presumably returned the next version of itself
  */
trait Signal[I, O]
  extends (I => (Signal[I, O], O))
    with TPorcelainCompose[I, O]
    with TPorcelainExecute[I, O] {

  /**
    * this is the "plumbing" of the system. all other methods in this file are porcelain
    */
  def apply(i: I): (Signal[I, O], O)

}

object Signal {

  /**
    * does a computation that might not return a new value (so we just get the old one)
    */
  def maybe[I, O](last: O)(code: I => Option[O]): Signal[I, O] =
    (i: I) =>
      code(i) match {
        case None => (maybe(last)(code), last)
        case Some(next) => (maybe(next)(code), next)
      }


  /**
    * caches computed values in a LRU cache IFF the input doesn't change
    */
  final def cache[I, O](size: Int, from: I => O, values: List[(I, O)] = Nil): Signal[I, O] =
    (i: I) =>
      (values.filter((_: (I, O))._1 == i) match {
        case List((_, value)) => value
        case Nil => from(i)
      }) match {
        case value: O =>
          val next: Signal[I, O] =
            cache(size, from, ((i, value) :: values.filterNot((_: (I, O))._1 == i)).take(size))

          (next, value)
      }

  /**
    * allows the same `base` signal to be "shared" without replicating computation for identical sequences of input
    */
  final def shared[I, O](base: Signal[I, O]): Signal[I, O] = {

    // class is used here to prevent double-wrapping
    class Shared extends Signal[I, O] {
      private val cache: Map[I, (Signal[I, O], O)] =
        Map[I, (Signal[I, O], O)]().withDefault {
          i: I =>
            base.apply(i) match {
              case (next: Signal[I, O], output: O) =>
                shared(next) -> output
            }
        }

      override def apply(i: I): (Signal[I, O], O) = cache(i)
    }

    // don't double wrap
    // - it's wasteful
    // - if a is wrapped to a'b and a'c then both are given input i; a(i) will be invoked twice - defeating the purpose
    base match {
      case _: Shared => base
      case _ => new Shared
    }
  }

  final def identity[I]: Signal[I, I] =
    new Signal[I, I] {
      override def apply(i: I): (Signal[I, I], I) = (this, i)
    }

  /**
    * make a signal from a function
    */
  implicit def stateless[I, O](function: I => O): Signal[I, O] =
    new Signal[I, O] {
      override def apply(i: I): (Signal[I, O], O) = (this, function(i))
    }

  /**
    * composes a signal from a primitive binary operation on their values
    */
  def binary[I, L, R, O](op: (L, R) => O): (Signal[I, L], Signal[I, R]) => Signal[I, O] =
    (l: Signal[I, L], r: Signal[I, R]) =>
      (i: I) => {
        val (ls, lv: L) = l apply i
        val (rs, rv: R) = r apply i

        (binary(op)(ls, rs), op(lv, rv))
      }

  /**
    * produces a signal that can see what its last value was.
    *
    * ... so ... maybe an increment?
    */
  def reusing[I, O](last: O)(code: (I, O) => O): Signal[I, O] =
    (i: I) => {
      val o = code(i, last)
      (reusing(o)(code), o)
    }

  /**
    * emits elements from the passed list on a periodic loop
    */
  def cycle[V](values: List[V]): Signal[Unit, V] = {

    // selects a valid index and emits it
    val signalIndex: Signal[Unit, Int] =
      reusing[Unit, Int](-1) {
        (_: Unit, i: Int) =>
          // applying % here prevents overflow
          (i + 1) % values.size
      }


    // selects a value at an index and emits it
    val signalValue: Signal[Int, V] =
      stateless(values.apply)

    //
    signalIndex | signalValue
  }

  /**
    * a constant signal just returns some constant value
    */
  implicit def constant[V](value: V): Signal[Unit, V] =
    new Signal[Unit, V] {
      override def apply(i: Unit): (Signal[Unit, V], V) = (this, value)
    }

  /**
    * Porcelain methods that builds on apply()
    */
  private[frp] trait TPorcelainExecute[I, O] {
    this: Signal[I, O] =>

    @deprecated
    final def seq(i: I*): O = (this << i.toStream)._2

    /**
      * run us on a stream of inputs and take the stream of outputs
      */
    final def !(stream: Iterable[I]): Stream[O] =
      stream match {
        case Empty => Empty

        case head #:: tail =>
          apply(head) match {
            case (next, data) =>
              data #:: (next ! tail)
          }

        case _ =>
          this ! stream.toStream
      }

    /**
      * run us on a stream of inputs and return only the final state
      */
    final def <<(stream: Iterable[I]): (Signal[I, O], O) =
      stream match {
        case Stream(last) =>
          apply(last)
        case head #:: tail =>
          apply(head)._1 << tail
        case _ =>
          this << stream.toStream
      }
  }

  /**
    * Porcelain methods to build on composing signals
    */
  private[frp] trait TPorcelainCompose[I, O] {
    this: Signal[I, O] =>

    /**
      * chains us with another signal into a new signal
      */
    final def |[R](next: Signal[O, R]): Signal[I, R] =
      (i: I) =>
        apply(i) match {
          case (left: Signal[I, O], o: O) =>
            next.apply(o) match {
              case (next: Signal[O, R], r: R) =>
                (left | next, r)
            }
        }

    /**
      * wraps a signal with a convertible input
      */
    final def wrap[A](w: A => I): Signal[A, O] =
      (i: A) => {
        val (s, v) = apply(w(i))
        (s.wrap(w), v)
      }

    /**
      * wraps a signal with a convertible output
      */
    final def cast[R](c: O => R): Signal[I, R] =
      (i: I) => {
        val (s, o) = apply(i)
        (s.cast(c), c(o))
      }

    /**
      * builds a LRU cache
      */
    final def cache(size: Int): Signal[I, O] = {
      def cacheSignal(from: Signal[I, O], values: List[(I, O)]): Signal[I, O] = {
        (i: I) => {
          (values.find((_: (I, O))._1 == i) match {
            case Some((_, data)) => (from, data)
            case None => from.apply(i)
          }) match {
            case (next, data) =>
              (cacheSignal(next, ((i, data) :: values.filterNot((_: (I, O))._1 == i)).take(size)), data)
          }
        }
      }

      cacheSignal(this, Nil)
    }
  }

}
