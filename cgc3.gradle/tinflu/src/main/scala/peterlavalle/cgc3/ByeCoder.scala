package peterlavalle.cgc3

/**
	* writes bytes to pretty C source
	*/
object ByeCoder {
	def apply(stream: Stream[Byte]): Stream[String] =
		stream.map(ByeCoder.apply)

	def apply(byte: Byte): String =
		s"((uint8_t)(0x${"%02X".format(byte)}))"
}
