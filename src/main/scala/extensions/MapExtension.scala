package extensions

object MapExtension {

  class MapExtensionImpl[TKey, TValue](map: Map[TKey, TValue]) {
    /**
      * GetOrElse with custom comparer
      * @param key
      * @param elseValue
      * @param filter
      * @return
      */
    def getOrElseWithCompare(key: TKey, elseValue: TValue, filter: (TKey, TKey) => Boolean): TValue = {
      map.keys.find(x => filter(x, key)) match {
        case Some(key) => map.get(key).head
        case None => elseValue
      }
    }
  }

  implicit def extendMap[TKey, TValue](map: Map[TKey, TValue]) = new MapExtensionImpl(map)
}

