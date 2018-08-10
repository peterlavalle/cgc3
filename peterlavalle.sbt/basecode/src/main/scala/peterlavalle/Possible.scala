package peterlavalle

/**
  * like "Option[T]" but with an error message
  *
  * @tparam T
  */
trait Possible[T] {

  def bind[V](operation: T => V): Possible[V]

  def open(handle: (String, Possible.StackTrace) => T): T

  def okay: Boolean

  def fail: Option[String]

  final def dumb: Option[T] =
    if (okay)
      Some(open)
    else
      None

  final def open: T = open { case (message, _) => throw new Exception(message) }
}

object Possible {
  type StackTrace = List[StackTraceElement]

  def okay[T](value: T): Possible[T] =
    new Possible[T] {
      override def bind[V](operation: T => V): Possible[V] =
        Possible.okay[V](operation(value))

      override def open(handle: (String, Possible.StackTrace) => T): T =
        value

      override def okay: Boolean = true

      override def fail: Option[String] = None
    }

  def fail[T](message: String, stackTrace: StackTrace = Thread.currentThread().getStackTrace.toList): Possible[T] =
    new Possible[T] {
      override def bind[V](operation: T => V): Possible[V] =
        Possible.fail[V](message)

      override def open(handle: (String, StackTrace) => T): T =
        handle(message, stackTrace)

      override def okay: Boolean = false

      override def fail: Option[String] = Some(message)
    }
}
