package peterlavalle

import java.io.{InputStream, OutputStream}

import scala.collection.immutable.Stream.Empty

trait TXOutputStream {

  implicit class WrappedOutputStream[O <: OutputStream](outputStream: O) {

    def append(data: Stream[Byte]): O =
      append(data, 64 /* take 64 byte blocks */)

    def append(data: Stream[Byte], size: Int): O =
      data.splitAt(size) match {
        case (Empty, _) =>
          require(data.isEmpty)
          outputStream

        case (left, tail) =>
          require(left.nonEmpty)

          outputStream.write(left.toArray)

          append(tail, (size * 1.14).toInt)
      }

    def <<(from: InputStream): O = {

      val data: Array[Byte] = Array.ofDim[Byte](128)

      def recu(read: Int): O =
        read match {
          case -1 =>
            from.close()
            outputStream

          case _ =>
            outputStream.write(data, 0, read)
            recu(from read data)
        }

      recu(from read data)
    }
  }

}
