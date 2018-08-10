package peterlavalle.cgc3

import java.io.{ByteArrayOutputStream, InputStream}
import java.util.zip.{Deflater, DeflaterOutputStream}

import lu.luz.jzopfli.ZopfliH.{ZopfliFormat, ZopfliOptions}

object Compress {

	def deflateChunks(originalData: Stream[Array[Byte]]): Iterable[Array[Byte]] = {
		new Iterable[Array[Byte]] {
			override def iterator: Iterator[Array[Byte]] =
				new Iterator[Array[Byte]] {

					val deflater: Deflater = new Deflater(Deflater.BEST_COMPRESSION)
					val outputStream: ByteArrayOutputStream = new ByteArrayOutputStream()
					val deflaterOutputStream: DeflaterOutputStream = new DeflaterOutputStream(outputStream, deflater)
					val inputStream: Iterator[Array[Byte]] = originalData.iterator

					override def hasNext: Boolean = {

						// push some bytes
						if (inputStream.isEmpty)
							deflaterOutputStream.flush()
						else
							deflaterOutputStream.write(inputStream.next())

						0 != outputStream.size()
					}

					override def next(): Array[Byte] = {
						require(hasNext)
						val next: Array[Byte] = outputStream.toByteArray
						outputStream.reset()
						next
					}
				}
		}
	}

	def chunkit(inputStream: InputStream)(implicit readChunkSize: Int = 512): Stream[Array[Byte]] = {
		val buffer: Array[Byte] = Array.ofDim[Byte](readChunkSize)
		inputStream.read(buffer) match {
			case -1 | 0 => Stream.Empty
			case read: Int =>
				(if (read != readChunkSize)
					buffer.take(read)
				else
					buffer) #:: chunkit(inputStream)

		}
	}

	def mergeFlat(chunks: Stream[Array[Byte]]): Array[Byte] = {

		def mergeFlat(done: Array[Byte], todo: Stream[Array[Byte]]): Array[Byte] =
			todo match {
				case Stream.Empty => done

				case head #:: tail =>

					val next: Array[Byte] = Array.ofDim[Byte](done.length + head.length)

					// copy the data from the done array
					done.indices.foreach {
						i: Int =>
							next(i) = done(i)
					}

					//
					head.indices.foreach {
						i: Int =>
							next(done.length + i) = head(i)
					}

					// recur
					mergeFlat(next, tail)
			}

		mergeFlat(
			Array.ofDim[Byte](0),
			chunks
		)
	}

	def zopfli(originalArray: Array[Byte], iterations: Int, blockSplittingMax: Int): Iterable[Byte] = {

		require(
			3 < iterations,
			"No! Want more loopz!"
		)

		val zopfliOptions: ZopfliOptions = new ZopfliOptions()
		zopfliOptions.verbose = false
		zopfliOptions.verbose_more = false
		zopfliOptions.numiterations = iterations
		zopfliOptions.blocksplitting = 0 < blockSplittingMax
		zopfliOptions.blocksplittinglast = false
		zopfliOptions.blocksplittingmax = blockSplittingMax

		val outData: Array[Array[Byte]] = Array[Array[Byte]](Array[Byte](0))
		val outSize: Array[Int] = Array[Int](0)

		import lu.luz.jzopfli.Zopfli_lib

		try {
			Zopfli_lib.ZopfliCompress(
				zopfliOptions,
				ZopfliFormat.ZOPFLI_FORMAT_DEFLATE,
				originalArray,
				originalArray.length,
				outData,
				outSize
			)
			require(1 == outData.length)
			require(1 == outSize.length)
		} catch {
			case e: Throwable =>
				throw new RuntimeException(
					"Failure durring compression",
					e
				)
		}

		try {
			(0 until outSize(0)).map(outData(0))
		} catch {
			case e: Throwable =>
				throw new RuntimeException(
					"Failure while converting data to an array",
					e
				)
		}
	}
}
