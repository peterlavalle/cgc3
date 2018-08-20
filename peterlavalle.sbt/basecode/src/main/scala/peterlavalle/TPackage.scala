package peterlavalle

import org.codehaus.plexus.util.cli.Commandline

import scala.collection.convert.{WrapAsJava, WrapAsScala}
import scala.reflect.ClassTag

trait TPackage
  extends TContextDependantMapping
    with PBoolean
    with PChain
    with PFile
    with PFunction
    with PIterable
    with PObject
    with PString
    with TXCommandLine
    with TXDate
    with TXInputStream
    with TXMap
    with TXOutputStream
    with TXThrowable
    with TXWriter
    with WrapAsScala with WrapAsJava {

  implicit class WrapClassTag[T](thisTag: ClassTag[T]) {
    def isAssignableTo[V](implicit themTag: ClassTag[V]): Boolean =
      themTag.runtimeClass.isAssignableFrom(thisTag.runtimeClass)
  }

  implicit def requyre[E <: Exception](condition: Boolean, messsage: => String)(implicit tag: ClassTag[E]): Unit = {
    if (!condition)
      throw tag.runtimeClass.getConstructor(classOf[String]).newInstance(messsage).asInstanceOf[E]
  }


  def STUB: Nothing = STUB(null)

  /**
    * this prints a stack trace than closes the program; useful when you're trying to reverse engineer an interface whose exceptions are being caught
    *
    * @return Nothing
    */
  def STUB(message: String): Nothing = {
    val runtimeException: RuntimeException =
      if (null != message)
        new RuntimeException(message)
      else
        new RuntimeException()


    // change the stack trace
    runtimeException.setStackTrace {
      runtimeException.getStackTrace
        // burn off anything referring to this method
        .dropWhile((_: StackTraceElement).getMethodName.matches("STUB\\$?"))
    }

    // print the stack trace
    runtimeException.printStackTrace(System.out)
    System.out.flush()
    Thread.sleep(10)
    runtimeException.printStackTrace(System.err)
    System.err.flush()

    // now exit
    System.exit(-1)
    throw runtimeException
  }

  def ??? : Nothing = {
    val base = new NotImplementedError
    base.setStackTrace(base.getStackTrace.tail)

    while (base.getStackTrace.head.toString.trim.matches(".*\\$qmark\\$qmark\\$qmark\\$?\\([^\\)]+\\)$"))
      base.setStackTrace(base.getStackTrace.tail)

    val notImplementedError = new NotImplementedError(s"${base.getMessage} @ ${base.getStackTrace.head.toString.trim}")
    notImplementedError.setStackTrace(base.getStackTrace)
    throw notImplementedError
  }

  def FAIL(message: => String): Nothing = {
    val runtimeFailureException = new RuntimeException(message)

    runtimeFailureException.setStackTrace {
      runtimeFailureException.getStackTrace
        .dropWhile((f: StackTraceElement) => f.getMethodName == "FAIL" || f.getMethodName == "FAIL$")
    }

    throw runtimeFailureException
  }

  def TODO(message: => String): Unit = {
    val runtimeFailureException = new RuntimeException(message)

    runtimeFailureException.setStackTrace {
      runtimeFailureException.getStackTrace
        .dropWhile((f: StackTraceElement) => f.getMethodName == "TODO" || f.getMethodName == "TODO$")
        .take(1)
    }

    System.err.println(
      message + "\n\t" + runtimeFailureException.getStackTrace.head
    )
  }


  implicit class pCommandline(commandLine: Commandline) {
    def requireSuccess: Iterable[String] =
      commandLine.text {
        case (0, out, Nil) =>
          out

        case (r, out, err) =>
          sys.error(s"r = $r")
      }
  }

}
