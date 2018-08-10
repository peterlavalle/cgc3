package peterlavalle

import java.util

class MemoMap[K, V](make: K => V) extends (K => V) {
	private val leaf$map = new util.HashMap[K, V]()

	def ?(k: K): Boolean = leaf$map containsKey k

	def apply(key: K): V =
		if (leaf$map.containsKey(key))
			leaf$map(key)
		else {
			leaf$map.put(key, make(key))
			apply(key)
		}
}

object MemoMap {
	def of[K, V](code: K => V): MemoMap[K, V] =
		new MemoMap(code)
}
