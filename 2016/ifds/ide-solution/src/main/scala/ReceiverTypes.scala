import org.opalj.br.ObjectType
import heros.EdgeFunction
import heros.edgefunc.AllTop
import heros.edgefunc.EdgeIdentity
import scala.None
import org.opalj.br.ClassHierarchy

class ReceiverTypes(
        upperTypeOfRegister: Map[Int, ObjectType] = Map.empty) {

    def meet(map: Map[Int, ObjectType], classHierarchy: ClassHierarchy): ReceiverTypes = {
        val m = Utility.meet(upperTypeOfRegister, map, classHierarchy)
        m.map(new ReceiverTypes(_)).getOrElse(Top)
    }
}

object Top extends ReceiverTypes {

}

object Utility {

    def subtype(tpe1: ObjectType, tpe2: ObjectType, classHierarchy: ClassHierarchy): Option[ObjectType] = {
        if (tpe1 == tpe2)
            Some(tpe1)
        else if (classHierarchy.isSubtypeOf(tpe1, tpe2).isYes)
            Some(tpe1)
        else if (classHierarchy.isSubtypeOf(tpe2, tpe1).isYes)
            Some(tpe2)
        else
            None
    }

    def meet(map1: Map[Int, ObjectType], map2: Map[Int, ObjectType], classHierarchy: ClassHierarchy): Option[Map[Int, ObjectType]] = {
        val r = for (
            key ← (map1.keySet ++ map2.keySet)
        ) yield {
            if (map1.contains(key)) {
                if (map2.contains(key)) {
                    val s = subtype(map1(key), map2(key), classHierarchy)
                    if (s.isDefined)
                        key -> s.get
                    else
                        return None
                } else
                    key -> map1(key)
            } else
                key -> map2(key)
        }
        Some(r.toMap)
    }

    def join(map1: Map[Int, ObjectType], map2: Map[Int, ObjectType], classHierarchy: ClassHierarchy): Map[Int, ObjectType] = {
        val r = for (
            key ← (map1.keySet ++ map2.keySet) if map1.contains(key) && map2.contains(key)
        ) yield {
            key -> classHierarchy.joinObjectTypesUntilSingleUpperBound(map1(key), map2(key), true)
        }
        r.toMap
    }
}

case class MapRegisterToTypeFunction(
        upperTypeOfRegister: Map[Int, ObjectType],
        classHierarchy: ClassHierarchy) extends EdgeFunction[ReceiverTypes] {

    def composeWith(edgeFn: heros.EdgeFunction[ReceiverTypes]): heros.EdgeFunction[ReceiverTypes] = {
        edgeFn match {
            case top: AllTop[ReceiverTypes]      ⇒ top
            case id: EdgeIdentity[ReceiverTypes] ⇒ this
            case MapRegisterToTypeFunction(map, _) ⇒
                val m = Utility.meet(map, upperTypeOfRegister, classHierarchy)
                if (m.isDefined)
                    MapRegisterToTypeFunction(m.get, classHierarchy)
                else
                    new AllTop(Top)
        }
    }

    def computeTarget(rec: ReceiverTypes): ReceiverTypes = rec.meet(upperTypeOfRegister, classHierarchy)

    def equalTo(edgeFn: heros.EdgeFunction[ReceiverTypes]): Boolean = equals(edgeFn)

    def joinWith(edgeFn: heros.EdgeFunction[ReceiverTypes]): heros.EdgeFunction[ReceiverTypes] = {
        edgeFn match {
            case top: AllTop[ReceiverTypes]        ⇒ top
            case id: EdgeIdentity[ReceiverTypes]   ⇒ id
            case MapRegisterToTypeFunction(map, _) ⇒ MapRegisterToTypeFunction(Utility.join(map, upperTypeOfRegister, classHierarchy), classHierarchy)
        }
    }

    override def toString(): String = {
        upperTypeOfRegister.toString()
    }
}

object MapRegisterToTypeFunction {
    def apply(registerIndex: Int, receiverType: ObjectType, classHierarchy: ClassHierarchy): MapRegisterToTypeFunction = {
        MapRegisterToTypeFunction(Map(registerIndex -> receiverType), classHierarchy)
    }
}

object MapSingleRegister {
    /**
     * Assumes MapRegisterToTypeFunction maps a single register identified by its index to some type and returns both values as a pair.
     * If the assumption does not hold it returns None.
     */
    def unapply(mapFn: MapRegisterToTypeFunction): Option[Tuple2[Int, ObjectType]] = {
        if (mapFn.upperTypeOfRegister.size == 1) {
            val key = mapFn.upperTypeOfRegister.keys.last
            Some((key, mapFn.upperTypeOfRegister(key)))
        } else
            None
    }
}