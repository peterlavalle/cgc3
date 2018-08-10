package peterlavalle

import java.io.{File, FileOutputStream, StringWriter}

class OverWriter(file: File) extends StringWriter {
  def closeFile: File = {
    close()
    file
  }

  override def close(): Unit = {
    val file = this.file.getAbsoluteFile

    flush()

    require(file.getParentFile.exists() || file.getParentFile.mkdirs())

    val newText: String = toString.replaceAll("\r?\n", "\n")

    if (!file.exists() || newText != file.makeString.replaceAll("\r?\n", "\n")) {
      if (file.exists()) {
        require(file.delete(), s"Failed to delete a file for overwrite `${file.getAbsolutePath}`")
        require(!file.exists(), s"Delete failed for a file that needed to be overwritten `${file.getAbsolutePath}`")
      }
      val stream = new FileOutputStream(file)
      stream.write(newText.getBytes("UTF-8"))
      stream.close()
    }

    super.close()
  }
}
