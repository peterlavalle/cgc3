package peterlavalle

import scala.reflect.ClassTag

/**
  * This is (or should be) a sort of lazy value that has to be explicitly initialised
  */
trait Later[T] {

  def ??[V](handle: Option[T] => V): V

  def wrap[O](wrapper: T => O)(implicit oTag: ClassTag[T], iTag: ClassTag[O]): Later[O] =
    new peterlavalle.Later.PassThrough[O, T](this, wrapper)

  /**
    * quick and dirty use of .map()
    */
  def filter[I](query: I => Boolean)(implicit iTag: ClassTag[I]): Iterable[I] =
    map[I, (Boolean, I)](i => (query(i), i)).filter(_._1).map(_._2)

  /**
    * assumes that we're getting an instance of an iterable; this does a mapping
    */
  def map[I, O](operation: I => O)(implicit iTag: ClassTag[I], oTag: ClassTag[O]): Iterable[O] =
    get match {
      case iterable: Iterable[I] =>
        iterable map operation
    }

  def get: T = this ? ((v: T) => v)

  def ?[V](handle: T => V): V =
    this ?? {
      case Some(value) =>
        handle(value)
      case None =>
        throw new Later.NotReadyException(this)
    }
}

object Later {

  def Stub[T](message: String = "missing"): Later[T] = {
    new Later[T] {
      override def ??[V](handle: Option[T] => V): V = {
        FAIL(message)
      }
    }
  }

  trait T {

    import scala.language.implicitConversions
    import scala.languageFeature.implicitConversions

    implicit def nowLater[T](later: Later[T]): T = later.get
  }

  class NotReadyException(message: String, val later: AnyRef) extends Exception(message) {
    def this(which: AnyRef) = this("a later is not ready", which)
  }

  class PassThrough[O, I](real: Later[I], wrapper: I => O)(implicit oTag: ClassTag[O], iTag: ClassTag[I]) extends Later[O] {
    override def ??[V](handle: Option[O] => V) =
      real ?? {
        case Some(o: I) =>
          handle(Some(wrapper(o)))
      }
  }

  class SetOnce[T](message: => String = null) {
    lazy val later: Later[T] =
      new Later[T] {
        override def ??[V](handle: (Option[T]) => V): V =
          try {
            handle(value)
          } catch {
            case e: Later.NotReadyException if null != message && this == e.later =>
              throw new NotReadyException(message)
          }
      }
    private var value: Option[T] = None

    def :=(value: T): Unit =
      this.value match {
        case None =>
          this.value = Some(value)
      }
  }

  object cast extends T

}
