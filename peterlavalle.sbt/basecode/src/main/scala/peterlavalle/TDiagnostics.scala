package peterlavalle


trait TDiagnostics {

  def TODO(message: String): Unit = {
    System.out.flush()
    System.err.flush()
    System.err.println(
      s"TODO: $message ${TDiagnostics.caller}"
    )
  }

  def STUB(message: String = "STUB"): Nothing = {
    System.out.flush()
    System.err.flush()
    val notImplementedError: NotImplementedError = new NotImplementedError(s"STUB: $message ${TDiagnostics.caller}")
    notImplementedError.setStackTrace(TDiagnostics.strippedTrace.toArray)
    throw notImplementedError
  }

  def TEST[T](check: T => Boolean, message: => String = "failed")(code: => T): T = {
    System.out.flush()
    System.err.flush()
    val result: T = code
    if (check(result))
      code
    else {
      val illegalArgumentException: IllegalArgumentException = new IllegalArgumentException(s"TEST: $message ${TDiagnostics.caller}")
      illegalArgumentException.setStackTrace(TDiagnostics.strippedTrace.toArray)
      throw illegalArgumentException
    }
  }
}

object TDiagnostics extends TDiagnostics {
  val mine: String = s"${classOf[TDiagnostics].getSimpleName}.scala"

  // TODO; test if our filename has changed

  import scala.language.implicitConversions


  private def caller: String = {
    caller(strippedTrace)
  }

  private def caller(trace: List[StackTraceElement]): String = {
    trace.head match {
      case head: StackTraceElement =>
        s"(${head.getFileName}:${head.getLineNumber})"
      case _ =>
        FAIL(
          "SUPER WEIRD ERROR"
        )
    }
  }

  private def strippedTrace: List[StackTraceElement] =
    Thread.currentThread().getStackTrace
      .toList
      .dropWhile((_: StackTraceElement).getFileName != TDiagnostics.mine)
      .dropWhile((_: StackTraceElement).getFileName == TDiagnostics.mine)
}
