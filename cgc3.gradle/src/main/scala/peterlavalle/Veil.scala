package peterlavalle

/**
	* wraps around something to keep track of whether it's been seen
	*/
class Veil {
	private var seen: Boolean = false

	def read[O](code: => O): O = {
		seen = true
		code
	}

	def bind[O](code: => O): O = {
		require(!seen)
		code
	}
}

object Veil {

	def value[V](make: => V): Value[V] =
		new Value[V](make)

	class Value[V](var value: V) {
		private val veil = new Veil()

		def swap(code: V => V): Unit = {
			veil.bind {
				value = code(value)
			}
		}

		def once: V =
			veil.bind {
				veil.read {
					value
				}
			}

		def read: V = {
			veil.read {
				value
			}
		}

		def bind[O](code: V => O): O = {
			veil.bind {
				code(value)
			}
		}
	}

}
