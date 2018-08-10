package peterlavalle

/**
  * like Option[+A] but lazy
  */
trait Maybe[+T] {

  /**
    * Apply some operation to the value, and, return a new monad
    **/
  def bind[V](operation: T => V): Maybe[V]

  /**
    * if we're empty; produce the other one
    */
  def otherwise[E >: T](them: Maybe[E]): Maybe[E]

  /**
    * produce/convert this to a strict Option[T] in preparation for reading or matching it.
    */
  def some: Option[T]
}

object Maybe {

  def apply[T](readit: => T): Maybe[T] = new Just[T](readit)

  def unapply[T](arg: Maybe[T]): Option[T] = arg.some

  private class Just[T](read: => T) extends Maybe[T] {
    lazy final val some: Option[T] = Some(read)

    override final def otherwise[E >: T](them: Maybe[E]): Maybe[E] = this

    override final def bind[V](map: T => V): Maybe[V] =
      Maybe {
        map {
          some.get
        }
      }
  }

  case object Nope extends Maybe[Nothing] {
    override val some: Option[Nothing] = None

    override def bind[V](map: Nothing => V): Maybe[V] = Nope

    override def otherwise[E >: Nothing](them: Maybe[E]): Maybe[E] = them
  }

}
