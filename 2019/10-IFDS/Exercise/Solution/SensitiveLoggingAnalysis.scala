package org.opalj.exercise

import org.opalj.br.analyses.SomeProject
import org.opalj.br.DeclaredMethod
import org.opalj.br.ObjectType
import org.opalj.tac.fpcf.analyses.AbstractIFDSAnalysis
import org.opalj.tac.fpcf.analyses.Statement
import org.opalj.tac.fpcf.properties.IFDSProperty
import org.opalj.tac.fpcf.properties.IFDSPropertyMetaInformation
import org.opalj.tac.Assignment
import org.opalj.tac.Expr
import org.opalj.tac.Var
import org.opalj.tac.fpcf.analyses.AbstractIFDSAnalysis.V
import org.opalj.tac.GetStatic
import org.opalj.tac.GetField
import org.opalj.tac.PutField
import org.opalj.tac.PutStatic
import org.opalj.tac.ReturnValue
import org.opalj.tac.fpcf.analyses.AbstractIFDSFact
import org.opalj.fpcf.PropertyKey
import org.opalj.fpcf.PropertyStore
import org.opalj.tac.fpcf.analyses.IFDSAnalysis
import org.opalj.tac.ArrayLoad
import org.opalj.tac.ArrayStore
import org.opalj.tac.Stmt
import org.opalj.tac.fpcf.analyses.AbstractIFDSNullFact

sealed trait Fact extends AbstractIFDSFact
case object NullFact extends Fact with AbstractIFDSNullFact
case class Variable(index: Int) extends Fact
case class ArrayElement(index: Int, element: Int) extends Fact
case class StaticField(classType: ObjectType, fieldName: String) extends Fact
case class InstanceField(index: Int, classType: ObjectType, fieldName: String) extends Fact

class SensitiveLoggingAnalysis private (
        implicit
        val project: SomeProject
) extends AbstractIFDSAnalysis[Fact] {

    val UserType = ObjectType("util/User")
    val LoggerType = ObjectType("util/Logger")

    override val propertyKey: IFDSPropertyMetaInformation[Fact] = Taint

    override def createPropertyValue(result: Map[Statement, Set[Fact]]): IFDSProperty[Fact] = {
        new Taint(result)
    }

    override def normalFlow(stmt: Statement, succ: Statement, in: Set[Fact]): Set[Fact] =
        stmt.stmt.astID match {
            case Assignment.ASTID ⇒
                handleAssignment(stmt, stmt.stmt.asAssignment.expr, in)

            case ArrayStore.ASTID ⇒
                val store = stmt.stmt.asArrayStore
                val definedBy = store.arrayRef.asVar.definedBy
                val index = getConstValue(store.index, stmt.code)
                if (isTainted(store.value, in))
                    if (index.isDefined) // Taint known array index
                        // Instead of using an iterator, we are going to use internal iteration
                        // in ++ definedBy.iterator.map(ArrayElement(_, index.get))
                        definedBy.foldLeft(in) { (c, n) ⇒ c + ArrayElement(n, index.get) }
                    else // Taint whole array if index is unknown
                        // Instead of using an iterator, we are going to use internal iteration:
                        // in ++ definedBy.iterator.map(Variable)
                        definedBy.foldLeft(in) { (c, n) ⇒ c + Variable(n) }
                else in

            case PutStatic.ASTID ⇒
                val put = stmt.stmt.asPutStatic
                if (isTainted(put.value, in))
                    in + StaticField(put.declaringClass, put.name)
                else
                    in

            case PutField.ASTID ⇒
                val put = stmt.stmt.asPutField
                val definedBy = put.objRef.asVar.definedBy
                if (isTainted(put.value, in))
                    definedBy.foldLeft(in) { (in, defSite) ⇒
                        in + InstanceField(defSite, put.declaringClass, put.name)
                    }
                else
                    in

            case _ ⇒ in
        }

    /**
     * Returns true if the expression contains a taint.
     */
    def isTainted(expr: Expr[V], in: Set[Fact]): Boolean = {
        expr.isVar && in.exists {
            case Variable(index)            ⇒ expr.asVar.definedBy.contains(index)
            case ArrayElement(index, _)     ⇒ expr.asVar.definedBy.contains(index)
            case InstanceField(index, _, _) ⇒ expr.asVar.definedBy.contains(index)
            case _                          ⇒ false
        }
    }

    /**
     * Returns the constant int value of an expression if it exists, None otherwise.
     */
    def getConstValue(expr: Expr[V], code: Array[Stmt[V]]): Option[Int] = {
        if (expr.isIntConst) Some(expr.asIntConst.value)
        else if (expr.isVar) {
            val constVals = expr.asVar.definedBy.iterator.map[Option[Int]] { idx ⇒
                if (idx >= 0) {
                    val stmt = code(idx)
                    if (stmt.astID == Assignment.ASTID && stmt.asAssignment.expr.isIntConst)
                        Some(stmt.asAssignment.expr.asIntConst.value)
                    else
                        None
                } else None
            }.toIterable
            if (constVals.forall(option ⇒ option.isDefined && option.get == constVals.head.get))
                constVals.head
            else None
        } else None
    }

    def handleAssignment(stmt: Statement, expr: Expr[V], in: Set[Fact]): Set[Fact] =
        expr.astID match {
            case Var.ASTID ⇒
                // This path is not used if the representation is in standard SSA-like form.
                // It is NOT optimized!
                val newTaint = in.collect {
                    case Variable(index) if expr.asVar.definedBy.contains(index) ⇒
                        Some(Variable(stmt.index))
                    case ArrayElement(index, taintIndex) if expr.asVar.definedBy.contains(index) ⇒
                        Some(ArrayElement(stmt.index, taintIndex))
                    case _ ⇒ None
                }.flatten
                in ++ newTaint

            case ArrayLoad.ASTID ⇒
                val load = expr.asArrayLoad
                if (in.exists {
                    // The specific array element may be tainted
                    case ArrayElement(index, taintedIndex) ⇒
                        val element = getConstValue(load.index, stmt.code)
                        load.arrayRef.asVar.definedBy.contains(index) &&
                            (element.isEmpty || taintedIndex == element.get)
                    // Or the whole array
                    case Variable(index) ⇒ load.arrayRef.asVar.definedBy.contains(index)
                    case _               ⇒ false
                })
                    in + Variable(stmt.index)
                else
                    in

            case GetStatic.ASTID ⇒
                val get = expr.asGetStatic
                if (in.contains(StaticField(get.declaringClass, get.name)))
                    in + Variable(stmt.index)
                else
                    in

            case GetField.ASTID ⇒
                val get = expr.asGetField
                if (in.exists {
                    // The specific field may be tainted
                    case InstanceField(index, _, taintedField) ⇒
                        taintedField == get.name && get.objRef.asVar.definedBy.contains(index)
                    // Or the whole object
                    case Variable(index) ⇒ get.objRef.asVar.definedBy.contains(index)
                    case _               ⇒ false
                })
                    in + Variable(stmt.index)
                else
                    in

            case _ ⇒ in
        }

    override def callFlow(
        stmt:   Statement,
        callee: DeclaredMethod,
        in:     Set[Fact]
    ): Set[Fact] = {
        val call = asCall(stmt.stmt)
        val allParams = call.allParams
        var facts = Set.empty[Fact]
        in.foreach {
            case Variable(index) ⇒ // Taint formal parameter if actual parameter is tainted
                allParams.iterator.zipWithIndex.foreach {
                    case (param, pIndex) if param.asVar.definedBy.contains(index) ⇒
                        facts += Variable(paramToIndex(pIndex, !callee.definedMethod.isStatic))
                    case _ ⇒ // Nothing to do
                }

            case ArrayElement(index, taintedIndex) ⇒
                // Taint element of formal parameter if element of actual parameter is tainted
                allParams.zipWithIndex.collect {
                    case (param, pIndex) if param.asVar.definedBy.contains(index) ⇒
                        ArrayElement(paramToIndex(pIndex, !callee.definedMethod.isStatic), taintedIndex)
                }

            case InstanceField(index, declClass, taintedField) ⇒
                // Taint field of formal parameter if field of actual parameter is tainted
                // Only if the formal parameter is of a type that may have that field!
                allParams.iterator.zipWithIndex.foreach {
                    case (param, pIndex) if param.asVar.definedBy.contains(index) &&
                        (paramToIndex(pIndex, !callee.definedMethod.isStatic) != -1 ||
                            classHierarchy.isSubtypeOf(declClass, callee.declaringClassType)) ⇒
                        facts += InstanceField(paramToIndex(pIndex, !callee.definedMethod.isStatic), declClass, taintedField)
                    case _ ⇒ // Nothing to do
                }

            case sf: StaticField ⇒
                facts += sf

            case _ ⇒ // Nothing to do
        }
        facts
    }

    override def returnFlow(
        stmt:   Statement,
        callee: DeclaredMethod,
        exit:   Statement,
        succ:   Statement,
        in:     Set[Fact]
    ): Set[Fact] = {
            val call = asCall(stmt.stmt)
            val allParams = call.allParams
            var flows: Set[Fact] = Set.empty
            in.foreach {
                case ArrayElement(index, taintedIndex) if index < 0 && index > -100 ⇒
                    // Taint element of actual parameter if element of formal parameter is tainted
                    val param =
                        allParams(paramToIndex(index, !callee.definedMethod.isStatic))
                    flows ++= param.asVar.definedBy.iterator.map(ArrayElement(_, taintedIndex))

                case InstanceField(index, declClass, taintedField) if index < 0 && index > -255 ⇒
                    // Taint field of actual parameter if field of formal parameter is tainted
                    val param = allParams(paramToIndex(index, !callee.definedMethod.isStatic))
                    param.asVar.definedBy.foreach { defSite ⇒
                        flows += InstanceField(defSite, declClass, taintedField)
                    }

                case sf: StaticField ⇒
                    flows += sf

                case _ ⇒
            }

            // Propagate taints of the return value
            if (exit.stmt.astID == ReturnValue.ASTID && stmt.stmt.astID == Assignment.ASTID) {
                val returnValue = exit.stmt.asReturnValue.expr.asVar
                in.foreach {
                    case Variable(index) if returnValue.definedBy.contains(index) ⇒
                        flows += Variable(stmt.index)
                    case ArrayElement(index, taintedIndex) if returnValue.definedBy.contains(index) ⇒
                        ArrayElement(stmt.index, taintedIndex)
                    case InstanceField(index, declClass, taintedField) if returnValue.definedBy.contains(index) ⇒
                        flows += InstanceField(stmt.index, declClass, taintedField)

                    case _ ⇒ // nothing to do
                }
            }

            flows
    }

    /**
     * Converts a parameter origin to the index in the parameter seq (and vice-versa).
     */
    def paramToIndex(param: Int, includeThis: Boolean): Int = (if (includeThis) -1 else -2) - param

    override def callToReturnFlow(stmt: Statement, succ: Statement, in: Set[Fact]): Set[Fact] = {
        val call = asCall(stmt.stmt)
        if (succ.node.isBasicBlock && call.name == "log" && (call.declaringClass eq LoggerType)) {
            if (in.exists {
                case Variable(index) ⇒ call.params.exists(p ⇒ p.asVar.definedBy.contains(index))
                case _               ⇒ false
            }) {
                System.out.println(s"VIOLATION in Method ${stmt.method.toJava}")
            }
            in
        } else if (call.name == "getName" && (call.declaringClass eq UserType) && stmt.stmt.astID == Assignment.ASTID) {
            in + Variable(stmt.index)
        } else {
            in
        }
    }

    override def nativeCall(statement: Statement, callee: DeclaredMethod, successor: Statement, in: Set[Fact]): Set[Fact] = {
        Set.empty
    }

    override val entryPoints: Map[DeclaredMethod, Fact] =
        p.allMethodsWithBody.collect {
            case m if m.isPublic ⇒ declaredMethods(m) → NullFact
        }.toMap

}

object SensitiveLoggingAnalysis extends IFDSAnalysis[Fact] {
    override def init(p: SomeProject, ps: PropertyStore) = new SensitiveLoggingAnalysis()(p)

    override def property: IFDSPropertyMetaInformation[Fact] = Taint
}

class Taint(val flows: Map[Statement, Set[Fact]]) extends IFDSProperty[Fact] {

    override type Self = Taint

    def key: PropertyKey[Taint] = Taint.key
}

object Taint extends IFDSPropertyMetaInformation[Fact] {
    override type Self = Taint

    val key: PropertyKey[Taint] = PropertyKey.create(
        "SensitiveLoggingTaint",
        new Taint(Map.empty)
    )
}
