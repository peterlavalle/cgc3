package peterlavalle

import java.io.File
import java.net.URL
import java.nio.file.Files
import java.util
import java.util.zip.{ZipEntry, ZipFile}

import scala.sys.process._

case class DumpCache(root: File) {
  def apply(url: String): File =
    apply(url, url.replaceAll("[_[^\\w]]+", "_"))

  def apply(url: String, path: String): File = {

    val dir: File = root / path

    if (!dir.exists()) {

      val zip: File = File.createTempFile(path, ".zip")

      DumpCache.download(
        url,
        zip
      )

      DumpCache.extract(
        zip,
        dir
      )

      require(zip.delete())
    }

    require(dir.exists())
    dir
  }
}

object DumpCache {
  def download(url: String, zip: File): Unit = {
    new URL(url) #> zip.getParentFile.EnsureExists !!
  }

  def extract(zip: File, dir: File): Unit = {
    val zipFile: ZipFile = new ZipFile(zip)

    def recu(entries: util.Enumeration[_]): Unit =
      if (!entries.hasMoreElements) {
        zipFile.close()
      } else {
        entries.nextElement() match {
          case next: ZipEntry =>
            if (!next.isDirectory)
              Files.copy(
                zipFile.getInputStream(next),
                (dir / next.getName).getParentFile.EnsureExists.toPath
              )
            recu(entries)
        }
      }

    zipFile.entries() match {
      case entries =>
        recu(
          entries
        )
    }
  }
}
