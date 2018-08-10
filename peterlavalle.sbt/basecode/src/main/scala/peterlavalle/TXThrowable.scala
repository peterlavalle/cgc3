package peterlavalle

import scala.collection.immutable.Stream.Empty

trait TXThrowable {

  implicit class WrappedThrowable2[T <: Throwable](throwable: T) {

    def trimTrace(count: Int = 20): T = {
      if (null != throwable.getCause)
        throwable.getCause.trimTrace(count)

      throwable.setStackTrace(
        throwable.getStackTrace.take(20)
      )
      throwable
    }

    def weanTrace: T = {

      if (null != throwable.getCause)
        throwable.getCause.weanTrace

      def wean(done: List[StackTraceElement], todo: Stream[StackTraceElement]): Unit =

        todo match {
          case Empty =>
            throwable.setStackTrace(
              done.reverse.toArray
            )
          case Stream(last) =>
            wean(
              last :: done,
              Stream.Empty
            )
          case head #:: next #:: tail =>
            if (head.getFileName == next.getFileName)
              wean(
                done,
                head #:: tail
              )
            else
              wean(
                head :: done,
                next #:: tail
              )
        }

      wean(
        Nil,
        throwable.getStackTrace.toStream
      )

      throwable
    }
  }

}
