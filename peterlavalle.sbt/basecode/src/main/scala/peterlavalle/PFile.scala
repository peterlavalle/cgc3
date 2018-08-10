package peterlavalle

import java.awt.Desktop
import java.io.{File, _}

import org.codehaus.plexus.util.cli.Commandline

import scala.collection.immutable.Stream.Empty
import scala.io.Source
import scala.language.postfixOps

trait PFile {

  implicit class pFile(from: File) {
    val file: File = {
      val absoluteFile: File = from.getAbsoluteFile
      if (from != absoluteFile)
        absoluteFile
      else
        from
    }

    def AbsolutePath: String =
      file.getAbsolutePath.replace('\\', '/')

    def Chain(out: String => Unit, err: String => Unit)(commands: Iterable[Any]*): Boolean =
      Chain(
        out,
        err,
        {
          case (_: Commandline, 0) =>
            true
        }
      )(commands: _ *)

    def Chain(out: String => Unit, err: String => Unit, success: (Commandline, Int) => Boolean)(commands: Iterable[Any]*): Boolean =
      commands.toList.map((_: Iterable[Any]).toList).foldLeft(true) {
        case (passed, program :: args) =>
          if (!passed)
            passed
          else {

            // generate the initial Commandline
            val commandline: Commandline =
              program match {
                case path: String =>
                  file.Shell(path)
                case file: File =>
                  file.Shell(file)
              }

            // add all args
            args.foreach(commandline.newArg)

            // invoke and check
            success(commandline, commandline.invoke(out, err))
          }
      }

    def explore(): Unit = {
      Desktop.getDesktop.open(file)
    }

    def EnsureParent: File = {
      file.getAbsoluteFile.getParentFile.EnsureExists
      file.getAbsoluteFile
    }

    def EnsureExists: File = {
      require(
        file.exists() || file.mkdirs()
      )
      file
    }

    def FreshFolder: File =
      if (!file.exists())
        file.EnsureExists
      else {
        requyre[Exception](file.Unlink, s"Couldn't delete `${file.AbsolutePath}`\n\t\t(... I want an empty folder with that name)")
        file.EnsureExists
      }

    def isNewer(other: File): Boolean =
      !(other.exists() && file.lastModified() < other.lastModified())

    def isOlder(stamp: Long): Boolean =
      file.lastModified() <= stamp

    def ls: List[String] =
      file.list() match {
        case null => Nil
        case list => list.toList
      }

    def makeString: String = {
      val fileReader = new FileReader(file)

      def recu: String = {
        val buffer = Array.ofDim[Char](512)
        fileReader.read(buffer) match {
          case -1 =>
            fileReader.close()
            ""
          case read =>
            require(0 < read)
            new String(buffer.take(read)) + recu
        }

      }

      recu
    }

    def ParentFile: File = file.getAbsoluteFile.getParentFile

    /**
      * relative path from `this` to `them`
      */
    def PathTo(them: File): String = {
      sys.error("There's a bug in here ...")

      def recu(m: List[String], t: List[String]): String =
        (m, t) match {
          case (Nil, some) =>
            some.reduce(_ + "/" + _)

          case ((mh :: mt), (th :: tt)) =>
            require(mh == th)
            recu(mt, tt)
        }

      recu(
        file.ParentFile.AbsolutePath.split("/").toList,
        them.AbsolutePath.split("/").toList
      )
    }

    def RequiredDirectory: File =
      if (file.isDirectory)
        file
      else
        sys.error(
          s"I needed the directory ${file.AbsolutePath}"
        )

    def Shell(command: File): Commandline =
      Shell(command.AbsolutePath)

    def sourceLines: Stream[String] =
      Source.fromFile(file).mkString.split("\r?\n").toStream


    def Shell(cmd: String, arg0: Any, args: Any*): Commandline =
      (arg0 :: args.toList).foldLeft(file.getAbsoluteFile.Shell(cmd))(_ newArg _)

    def Shell(command: String): Commandline = {

      require(null != command)

      val commandLine: Commandline = new Commandline()

      commandLine.setWorkingDirectory(file.AbsolutePath)

      commandLine.setExecutable(command)

      commandLine
    }

    def Unlink: Boolean =
      !file.exists() ||
        (!file.isDirectory && file.delete()) ||
        (file.isDirectory && file.listFiles().forall(_.Unlink) && file.delete())

    /**
      * relative path from `this` to `them`
      */
    def walkTo(them: File): String = {

      requyre[Exception](
        file.isDirectory,
        "I can only walk from directories"
      )

      def recu(m: List[String], t: List[String]): String =
        (m, t) match {

          case (Nil, t) =>
            t.reduce(_ + "/" + _)


          case ((mh :: mt), (th :: tt)) if mh == th =>
            recu(mt, tt)
          case _ =>
            ("../" * m.size) + t.reduce(_ + "/" + _)
        }

      recu(
        file.AbsolutePath.split("/").toList,
        them.AbsolutePath.split("/").toList
      )
    }

    def wipedDir: File = {
      if (file.exists()) {
        requyre[Exception](
          file.isDirectory,
          s"Not a directory ${file.AbsolutePath}"
        )

        requyre[Exception](
          file.Unlink,
          s"Failed to unlink ${file.AbsolutePath}"
        )
      }

      requyre[Exception](
        file.mkdirs(),
        s"Failed to create folder ${file.AbsolutePath}"
      )

      file
    }

    def !!(pattern: String): Stream[String] = this !!? ((_: String) matches pattern)

    def !!?(pattern: String => Boolean): Stream[String] = this **? (!pattern(_: String))

    def **(pattern: String): Stream[String] = this **? ((_: String) matches pattern)

    def **?(pattern: String => Boolean): Stream[String] =
      file.list().toStream.expand {
        path: String =>
          (file / path).list() match {
            case null => Empty
            case list => list.toStream.map(path + '/' + (_: String))
          }
      }.map {
        path: String =>
          if ((file / path).isDirectory)
            path + '/'
          else
            path
      }.filter(pattern)

    def /(path: String): File = {

      def recu(file: File, todo: List[String]): File =
        todo match {
          case ".." :: tail =>
            recu(file.getParentFile, tail)
          case next :: tail =>
            recu(
              new File(file, next),
              tail
            )
          case Nil =>
            file
        }

      recu(
        file,
        path.split("/").toList
      )
    }

    def >>(output: File): File = {
      output << file
      file
    }

    def <<(from: File): OutputStream =
      file << new FileInputStream(from)

    def <<(from: InputStream): FileOutputStream = {
      new FileOutputStream(file.EnsureParent) << from
    }

  }

}
