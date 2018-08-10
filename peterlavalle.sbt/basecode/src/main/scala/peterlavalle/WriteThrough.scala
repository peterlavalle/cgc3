package peterlavalle

import java.io.{File, FileWriter, Writer}

class WriteThrough(val file: File) extends Writer {

  require(file == file.getAbsoluteFile)

  requyre[Exception](
    file.getParentFile.exists() || file.getParentFile.mkdirs(),
    "can't make parent"
  )

  private val inner: FileWriter = new FileWriter(file)

  override def flush(): Unit = inner.flush()

  def closeFile(): File = {
    close()
    file
  }

  override def close(): Unit = inner.close()

  override def write(a: Array[Char], o: Int, l: Int): Unit = {
    inner.write(a, o, l)
    inner.flush()
  }
}
