package peterlavalle

import java.io.{ByteArrayInputStream, File, InputStream}

trait PString {

  implicit class pString(value: String) {

    def /(path: String): File =
      new File(new File(value).getAbsoluteFile, path)

    def reLine: String =
      value
        .split("[\r \t]*\n")
        .foldLeft("")((_: String) + (_: String) + "\n")

    def takeMark(mark: String): Option[(String, String)] = {
      if (!value.contains(mark))
        None
      else
        Some {
          val last: Int = value.indexOf(mark)
          value.splitAt(last + mark.length)
        }
    }

    def toInputStream: InputStream =
      new ByteArrayInputStream(value.getBytes())

    def stripTrim: String = value.stripMargin.trim + '\n'

    def tailTrim: String = value.stripMargin.replaceAll("\\s*$", "\n")

    def halt: Nothing = {

      val base = new RuntimeException
      base.setStackTrace(base.getStackTrace.tail.tail)

      val runtimeException = new RuntimeException(s"$value @ ${base.getStackTrace.head.toString.trim}")
      runtimeException.setStackTrace(base.getStackTrace)
      throw runtimeException
    }

    def toHash(form: String = "SHA-256"): String = {
      import java.nio.charset.StandardCharsets
      import java.security.MessageDigest
      new StringBuilder()
        .rollLeft(MessageDigest.getInstance(form).digest(value.getBytes(StandardCharsets.UTF_8))) {
          case (s, b: Byte) =>
            s.append(String.format("%02X", new java.lang.Byte(b)))
        }
        .toString()
    }

    def stripMarginTail: String =
      value
        .trim
        .stripMargin + "\n"

    def reIndent(indent: Int): String =
      value
        .replaceAll("\n|^", "$0" + ("\t" * indent))
        .replaceAll("[\t ]+$", "")

    def reReplace(pattern: String, replace: String): String = {
      val result: String = value.replaceAll(pattern, replace)

      if (result == value)
        value
      else
        result.reReplace(pattern, replace)
    }

  }

}
