package peterlavalle

import java.util

/**
  * this caches values when they're looked up and no sooner
  *
  * @param spawn used to create the value
  * @tparam K is the key type
  * @tparam V is the value type
  */
class LazyCache[K, V] private(spawn: K => V) extends (K => V) {

  private val cache: util.HashMap[K, V] = new java.util.HashMap[K, V]()

  def apply(key: K): V =
    cache.synchronized {
      if (!cache.containsKey(key))
        cache.put(key, spawn(key))
      cache.get(key)
    }

  @deprecated(
    "should not be used; it implies a stateful nature that I'm trying to avoid thinking about"
  )
  def ?(key: K): Boolean =
    cache.synchronized {
      cache.containsKey(key)
    }
}

object LazyCache {

  /**
    * construct a LazyCache with the passed lambda to spawn values
    *
    * @param spawn a value for a given key
    * @tparam K is the key type
    * @tparam V is the value type
    * @return an instance of LazyCache
    */
  def apply[K, V](spawn: K => V): LazyCache[K, V] = new LazyCache[K, V](spawn)

  def kinder[V](spawn: Class[_] => V): LazyCache[Class[_], V] =
    new LazyCache[Class[_], V](spawn)
}
