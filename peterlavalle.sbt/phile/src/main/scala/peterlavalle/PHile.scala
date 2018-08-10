package peterlavalle

import java.io._
import java.util.zip.{ZipEntry, ZipFile, ZipInputStream, ZipOutputStream}

/**
  * wraps a file system to make life a bit more predictable
  */
trait PHile {
  /**
    * selects some sub-content of a phile and makes it appear to be the only real content
    *
    * @param path the path within the phile
    * @return a new file containing old the named content
    */
  def subFolder(path: String): PHile =
    if (!path.endsWith("/"))
      subFolder(path + "/")
    else {
      val base = this
      new PHile {
        override def save(name: String, data: InputStream): Unit =
          base.save(path + name, data)

        override def list: Iterable[String] =
          base.list.filter((_: String).startsWith(path)).map((_: String).substring(path.length))

        override def open(name: String): InputStream =
          base.open(path + name)
      }
    }

  def matches(other: PHile): Boolean = {
    (list.toList.sorted == other.list.toList.sorted) && (!list.contains {
      name: String =>
        val t = (new ByteArrayOutputStream() << open(name)).toByteArray
        val o = (new ByteArrayOutputStream() << other.open(name)).toByteArray

        t.toList != o.toList
    })
  }

  def flat: Stream[(String, Array[Byte])] =
    list.toList.sorted.toStream.map {
      name: String =>
        name -> (new ByteArrayOutputStream() << open(name)).toByteArray
    }

  def list: Iterable[String]

  def open(name: String): InputStream

  def save(name: String, data: InputStream): Unit

  def filter(regex: String): PHile = {
    val self = this
    new PHile {
      def list: Iterable[String] =
        self.list.filter(_ matches regex)

      def open(name: String): InputStream = {
        require(list.contains(name))
        self.open(name)
      }

      def save(name: String, data: InputStream): Unit = {
        assume(name.matches(regex))
        self.save(name, data)
      }
    }
  }

  /**
    * dumps the contents to a temporary folder
    *
    * @return a temp folder containing the contents
    */
  def toDir: File = {
    val tmp = File.createTempFile("phile-dump", ".dir")
    toDir(tmp.FreshFolder)
  }

  def toDir(folder: File): File = {
    val file: File = folder.getAbsoluteFile
    list.foreach {
      path =>
        (new FileOutputStream((file / path).EnsureParent) << open(path))
          .close()
    }
    file
  }

  def include(root: File, regex: String): PHile =
    PHile.ofFolder(root).filter {
      name: String =>
        name matches regex
    }.link(this)

  def link(them: PHile): PHile = {
    val self = this
    new PHile {
      def list: Iterable[String] =
        (self.list.toStream ++ them.list).distinct

      def open(name: String): InputStream =
        if (self.list.contains(name))
          self.open(name)
        else
          them.open(name)

      def save(name: String, data: InputStream): Unit =
        self.save(name, data)
    }
  }

  def exclude(pattern: String): PHile =
    filter {
      name: String =>
        !(name matches pattern)
    }

  def filter(pattern: String => Boolean): PHile = {
    val base: PHile = this
    new PHile {
      override def save(name: String, data: InputStream): Unit = {
        assume(pattern(name))
        base.save(name, data)
      }

      override def list: Iterable[String] =
        base.list.filter(pattern)

      override def open(name: String): InputStream = {
        assume(pattern(name))
        base.open(name)
      }
    }
  }

  def toZip: File = {
    val file: File =
      File.createTempFile("toZip.", ".zip")

    list.foldLeft(new ZipOutputStream(new FileOutputStream(file))) {
      case (zipOutput, name: String) =>

        zipOutput.putNextEntry(new ZipEntry(name))

        val data: Array[Byte] =
          (new ByteArrayOutputStream() << open(name)).toByteArray

        zipOutput.write(data, 0, data.length)
        zipOutput.closeEntry()
        zipOutput
    }.close()

    file
  }

  def rename(regex: String, pattern: String): PHile = {
    val base = this

    val cached: BackCache[String, String] =
      BackCache[String, String] {
        text: String =>
          text.replaceAll(regex, pattern)
      }

    new PHile.ReadOnly {

      override def list: Iterable[String] =
        base.list.map {
          name =>
            if (name.matches(regex))
              cached(name)
            else
              name
        }

      override def open(name: String): InputStream =
        cached ? name match {
          case Some(raw) =>
            base open raw
          case _ =>
            base open name
        }
    }
  }
}

object PHile {

  val Fresh: PHile =
    new PHile {
      override def list: Iterable[String] = Nil

      override def open(name: String): InputStream =
        sys.error(
          "There's noting here"
        )

      override def save(name: String, data: InputStream): Unit =
        sys.error(
          "BEGONE!"
        )
    }

  def ofFolder(root: File): PHile =
    new PHile {
      require(!root.isFile)

      assume(root.exists() == root.isDirectory)

      def list: Stream[String] = root ** ".*"

      override def open(name: String): InputStream = {
        val file = root / name
        assume(file.exists() && file.isFile)
        new FileInputStream(file)
      }

      def save(name: String, data: InputStream): Unit =
        ((root / name) << data).close()
    }

  def ofZip(zip: File): PHile =
    new ReadOnly {

      override def list: Iterable[String] =
        new ZipFile(zip).entries().filterNot(_.isDirectory).toList.map {
          entry =>
            entry.getName
        }

      override def open(name: String): InputStream = {
        val zipFile = new ZipFile(zip)
        zipFile.getInputStream(zipFile.getEntry(name))
      }
    }

  def ofZip(inputStream: InputStream): PHile =
    new ReadOnly {

      val byteArray: Array[Byte] = (new ByteArrayOutputStream() << inputStream).toByteArray

      override def list: Iterable[String] = {
        val zipInputStream: ZipInputStream = new ZipInputStream(new ByteArrayInputStream(byteArray))
        Stream.continually(zipInputStream.getNextEntry).takeWhile(null != _)
          .filterNot(_.isDirectory)
          .map(_.getName)
      }

      override def open(name: String): InputStream = {

        def find(zipInputStream: ZipInputStream): InputStream =
          if (zipInputStream.getNextEntry.getName != name)
            find(zipInputStream)
          else {

            def scan(): Stream[Array[Byte]] = {
              val buffer: Array[Byte] = Array.ofDim[Byte](32)
              zipInputStream.read(buffer) match {
                case -1 => Stream.Empty

                case read =>
                  buffer.take(read) #:: scan()
              }
            }

            new ByteArrayInputStream(
              scan().flatten.toArray
            )
          }

        find(
          new ZipInputStream(new ByteArrayInputStream(byteArray))
        )
      }
    }

  trait ReadOnly extends PHile {
    override def save(name: String, data: InputStream): Unit =
      sys.error("This is a readonly phile")

  }

}
