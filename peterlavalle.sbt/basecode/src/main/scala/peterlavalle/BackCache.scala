package peterlavalle

import java.util

trait BackCache[K, V] extends (K => V) {
  /**
    * read a value for a given key
    *
    * @param key to read
    * @return the value for that key
    */
  def apply(key: K): V

  def ?(value: V): Option[K]
}

object BackCache {
  def apply[K, V](code: K => V): BackCache[K, V] =
    new BackCache[K, V] {
      val map = new util.HashMap[K, V]()

      /**
        * read a value for a given key
        *
        * @param key to read
        * @return the value for that key
        */
      override def apply(key: K): V = {
        if (!map.containsKey(key))
          map.put(key, code(key))

        map.get(key)
      }

      override def ?(value: V): Option[K] =
        map.entrySet().toArray.map {
          case entry: util.Map.Entry[K, V] =>
            (entry.getKey, entry.getValue)
        }.toList.filter(_._2 == value) match {
          case List((key: K, _)) =>
            Some(key)
          case Nil =>
            None
        }
    }
}
