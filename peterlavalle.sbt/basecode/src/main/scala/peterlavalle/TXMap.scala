package peterlavalle

trait TXMap {

  implicit class WrappedMap[K, V](map: Map[K, V]) {
    def toValueKey: Map[V, K] =
      for ((k, v) <- map) yield (v, k)
  }

}
