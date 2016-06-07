import org.opalj.br.ObjectType
import heros.EdgeFunction
import heros.edgefunc.AllTop
import heros.edgefunc.EdgeIdentity
import scala.None
import org.opalj.br.ClassHierarchy

class ReceiverTypes() {
  //TODO: Implement me
}

object Top extends ReceiverTypes {

}

case class MapRegisterToTypeFunction() extends EdgeFunction[ReceiverTypes] {

  def composeWith(edgeFn: heros.EdgeFunction[ReceiverTypes]): heros.EdgeFunction[ReceiverTypes] = ??? //TODO: Implement me

  def computeTarget(rec: ReceiverTypes): ReceiverTypes = ??? //TODO: Implement me

  def equalTo(edgeFn: heros.EdgeFunction[ReceiverTypes]): Boolean = equals(edgeFn)

  def joinWith(edgeFn: heros.EdgeFunction[ReceiverTypes]): heros.EdgeFunction[ReceiverTypes] = ??? //TODO: Implement me
}

object MapRegisterToTypeFunction {
  def apply(registerIndex: Int, receiverType: ObjectType, classHierarchy: ClassHierarchy): MapRegisterToTypeFunction = ??? //TODO: Implement me
}

object MapSingleRegister {
  /**
   * Assumes MapRegisterToTypeFunction maps a single register identified by its index to some type and returns both values as a pair.
   * If the assumption does not hold it returns None.
   */
  def unapply(mapFn: MapRegisterToTypeFunction): Option[Tuple2[Int, ObjectType]] = ??? //TODO: Implement me
}