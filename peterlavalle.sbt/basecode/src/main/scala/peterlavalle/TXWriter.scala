package peterlavalle

import java.io.Writer

trait TXWriter {

  implicit class WrappedWriter[W <: Writer](value: W) {

    def appund[T](monad: Option[T])(tostr: T => String): W =
      monad match {
        case None => value
        case Some(thing) =>
          value.appund(tostr(thing))
      }

    def appund[E](text: String): W =
      value.append(text).asInstanceOf[W]

    def appendSection[I](prefix: => String, contents: Iterable[I], suffix: String = "")(tostr: I => String): W =
      if (contents.isEmpty)
        value
      else {
        value
          .appund(prefix)
          .appund(contents)(tostr)
          .appund(suffix)
      }

    def appund[E](many: Iterable[E])(tostr: E => String): W =
      appund(many.iterator)(tostr)

    def appund[E](many: Iterator[E])(tostr: E => String): W =
      many.foldLeft(value)((w: W, e: E) => w.appund(tostr(e)))
  }

}
