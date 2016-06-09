import org.opalj.br.ObjectType
import heros.EdgeFunction
import heros.edgefunc.AllTop
import heros.edgefunc.EdgeIdentity
import scala.None
import org.opalj.br.ClassHierarchy

class ReceiverTypes() {
  //TODO: Implement me
  /**
   * This type represents the values that are passed along the computed edge functions in phase 2 of the algorithm.
   * However, we are only interested in reachability and do not require phase 2 to be executed at all.
   * Therefore, it is not strictly necessary to implement this class.
   */
}

object Top extends ReceiverTypes {

}

case class MapRegisterToTypeFunction(/* TODO: Implement me */) extends EdgeFunction[ReceiverTypes] {

  def composeWith(edgeFn: heros.EdgeFunction[ReceiverTypes]): heros.EdgeFunction[ReceiverTypes] = ??? //TODO: Implement me

  def computeTarget(rec: ReceiverTypes): ReceiverTypes = ??? //TODO: Implement me

  def equalTo(edgeFn: heros.EdgeFunction[ReceiverTypes]): Boolean = equals(edgeFn)

  def joinWith(edgeFn: heros.EdgeFunction[ReceiverTypes]): heros.EdgeFunction[ReceiverTypes] = ??? //TODO: Implement me
}

object MapRegisterToTypeFunction {
  def apply(registerIndex: Int, receiverType: ObjectType, classHierarchy: ClassHierarchy): MapRegisterToTypeFunction = ??? //TODO: Implement me 
  //Hint: you will need the ClassHierarchy object to compute common supertypes and common subtypes. 
  //For example, ClassHierarchy provides the methods joinObjectTypesUntilSingleUpperBound and isSupertypeOf
}

object MapSingleRegister {
  /**
   * Assumes MapRegisterToTypeFunction maps a single register identified by its index to some type and returns both values as a pair.
   * If the assumption does not hold it returns None.
   */
  def unapply(mapFn: MapRegisterToTypeFunction): Option[Tuple2[Int, ObjectType]] = ??? //TODO: Implement me
}