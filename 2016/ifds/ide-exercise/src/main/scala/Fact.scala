import org.opalj.br.ObjectType
import org.opalj.br.ComputationalTypeCategory
import org.opalj.br.Category2ComputationalTypeCategory
import org.opalj.br.Category1ComputationalTypeCategory
import org.opalj.br.ComputationalType

trait Fact {

}

object Zero extends Fact {
    override def toString() = "Zero"
}

case class FieldFact(declaringClass: ObjectType, fieldName: String) extends Fact {

}

/**
 * Represents a data-flow fact that tracks the operand stack.
 * The operand stack is represented by a list, whereas the top-most value is the first element (index: 0) of the list.
 * The value below the top-most value is the second element (index: 1) of the list, and so on.
 */
case class ValueFact(originPC: Int) extends Fact {

}

trait FactWithOperandStack extends Fact {

    val opStack: List[StackEntry]

    def copy(opStack: List[StackEntry]): FactWithOperandStack

    def pop(): FactWithOperandStack

    def push(entry: StackEntry): FactWithOperandStack
}

/**
 * A stack entry represents one entry of the operand stack.
 * It knows its computational type category and, if applicable, it knows from which register
 * the value was loaded.
 */
case class StackEntry(
        ctc: ComputationalTypeCategory,
        associatedRegister: Option[Int] = None) {
    override def toString() = {
        associatedRegister.getOrElse("?").toString()
    }
}
object StackEntry {

    def apply(ctc: ComputationalTypeCategory, registerIndex: Int): StackEntry = StackEntry(ctc, Some(registerIndex))

    def apply(ct: ComputationalType, registerIndex: Int): StackEntry = StackEntry(ct.computationalTypeCategory, Some(registerIndex))
}
object AssociatedRegister {
    def unapply(se: StackEntry): Option[Int] = se.associatedRegister
}

object UnknownCTC1Value extends StackEntry(Category1ComputationalTypeCategory, None)
object UnknownCTC2Value extends StackEntry(Category2ComputationalTypeCategory, None)

/**
 * A data-flow fact used to track a tainted value that has been stored on the operand stack.
 * The stack index is used to point to the specific entry of the operand stack that is considered tainted.
 */
case class OperandStackFact(stackIndex: Int, opStack: List[StackEntry]) extends FactWithOperandStack {

    def copy(opStack: List[StackEntry]): OperandStackFact = OperandStackFact(stackIndex, opStack)

    def pop() = OperandStackFact(stackIndex - 1, opStack.tail)

    def push(entry: StackEntry) = OperandStackFact(stackIndex + 1, entry :: opStack)

    override def toString() = {
        "Operand("+stackIndex+"; Stack("+opStack.mkString(",")+"))"
    }
}

/**
 * A data-flow fact used to track a tainted value that is stored in a register, identified by the register's index.
 */
case class RegisterFact(registerIndex: Int, opStack: List[StackEntry]) extends FactWithOperandStack {

    def copy(opStack: List[StackEntry]): RegisterFact = RegisterFact(registerIndex, opStack)

    def pop() = RegisterFact(registerIndex, opStack.tail)

    def push(entry: StackEntry) = RegisterFact(registerIndex, entry :: opStack)

    override def toString() = {
        "Register("+registerIndex+"; Stack("+opStack.mkString(",")+"))"
    }
}

/**
 * A data-flow fact representing that a tainted value has been stored in a field.
 * This is a field-based model, i.e., it does not represent the instance to which the field belongs.
 * Hence, this fact can be used for instance fields and static fields.
 */
case class FieldBasedFact(
        declaringClass: ObjectType,
        fieldName: String,
        opStack: List[StackEntry]) extends FactWithOperandStack {

    def copy(opStack: List[StackEntry]): FieldBasedFact = FieldBasedFact(declaringClass, fieldName, opStack)

    def pop() = FieldBasedFact(declaringClass, fieldName, opStack.tail)

    def push(entry: StackEntry) = FieldBasedFact(declaringClass, fieldName, entry :: opStack)

    override def toString() = {
        "Field("+fieldName+"; Stack("+opStack.mkString(",")+"))"
    }
}
