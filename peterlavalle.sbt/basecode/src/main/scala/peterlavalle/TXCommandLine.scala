package peterlavalle

import java.io.{File, InputStream}
import java.util

import org.codehaus.plexus.util.cli.{CommandLineUtils, Commandline, StreamConsumer}

trait TXCommandLine {

  implicit class LambdaStreamConsumer(lambda: String => Any) extends StreamConsumer {
    override def consumeLine(line: String): Unit = lambda
  }

  implicit class WrappedCommandLine(commandLine: Commandline) {
    def allArgs[I](args: Iterable[I])(flat: I => Any): Commandline =
      args.foldLeft(commandLine)(_ newArg flat(_))

    def text[O](code: (Int, Iterable[String], Iterable[String]) => O): O = {
      object Out extends StreamConsumer {
        val list = new util.LinkedList[String]

        override def consumeLine(line: String): Unit =
          list.add(line)
      }
      object Err extends StreamConsumer {

        val list = new util.LinkedList[String]

        override def consumeLine(line: String): Unit =
          list.add(line)
      }

      val r =
        commandLine.invoke(Out, Err)
      code(r,
        Out.list.toArray.map(_.asInstanceOf[String]),
        Err.list.toArray.map(_.asInstanceOf[String])
      )
    }

    def newArgs(values: Any*): Commandline =
      values.foldLeft(commandLine)(_ newArg _)

    def newArg(value: Any): Commandline = {
      value match {
        case f: File =>
          commandLine.createArg().setFile(f)
        case s: String =>
          commandLine.createArg().setValue(s.toString)
        case i: Int =>
          commandLine.createArg().setValue(i.toString)
        case later: Later[_] =>
          newArg(later.get)
      }
      commandLine
    }

    def invoke(out: String, err: String): Int =
      invoke(
        line => println(s"$out$line"),
        line => println(s"$err$line")
      )

    def invoke(out: String => Unit, err: String => Unit): Int =
      invoke(
        new StreamConsumer {
          override def consumeLine(line: String): Unit = out(line)
        },
        new StreamConsumer {
          override def consumeLine(line: String): Unit = err(line)
        }
      )

    def invoke(out: StreamConsumer, err: StreamConsumer): Int =
      CommandLineUtils.executeCommandLine(commandLine, out, err)

    @deprecated("use the old shell - don't need/use stdin since it made quick-testing in V$ a pain")
    def pipe[R](input: InputStream, err: String => Unit = System.err.println)(out: Any => Any): R =
      CommandLineUtils.executeCommandLine(
        commandLine,
        input,
        new StreamConsumer {
          override def consumeLine(line: String): Unit = out(line)
        },
        new StreamConsumer {
          override def consumeLine(line: String): Unit = err(line)
        }
      ) match {
        case r: Int =>
          out(r).asInstanceOf[R]
      }
  }

}
