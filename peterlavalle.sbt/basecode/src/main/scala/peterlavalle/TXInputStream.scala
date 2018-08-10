package peterlavalle

import java.io.InputStream

trait TXInputStream {

  implicit class WrappedInputStream[I <: InputStream](inputStream: I) {

    def toStream: Stream[Byte] = {

      def recu(): Stream[Byte] = {
        val buffer = Array.ofDim[Byte](64) // need this to be per-call since "grabbing the tail" would otherwise overwrite it

        // TODO ; enusre that this is, in fact, "lazy" then push it up to peterlavalle.sbt

        inputStream.read(buffer) match {
          case -1 =>
            inputStream.close()
            Stream.Empty

          case read =>
            (0 until read).toStream.map(buffer) ++ recu()
        }
      }

      recu()
    }
  }

}
