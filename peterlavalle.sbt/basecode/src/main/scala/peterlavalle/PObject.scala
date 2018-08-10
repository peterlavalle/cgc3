package peterlavalle

import scala.reflect.ClassTag

trait PObject {

  implicit class pObject[L <: Object](value: L) {
    def ifAs[T](code: T => Unit)(implicit classTag: ClassTag[T]): Unit =
      if (classTag.runtimeClass.isInstance(value))
        code(value.asInstanceOf[T])

    def rollLeft[N](iterable: Iterable[N])(operation: (L, N) => L): L =
      iterable.foldLeft(value)(operation)

    def notNull[E <: Exception](message: => String)(implicit classTag: ClassTag[E]): L = {
      requyre[E](
        null != value,
        message
      )
      value
    }

    def ifNullOrMap[V](otherwise: V)(lambda: L => V): V =
      if (null != value)
        lambda(value)
      else
        otherwise


  }

}
