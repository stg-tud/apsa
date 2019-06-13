package org.opalj.exercise

import org.opalj.br.analyses.SomeProject
import org.opalj.br.DeclaredMethod
import org.opalj.br.ObjectType
import org.opalj.tac.fpcf.analyses.AbstractIFDSAnalysis
import org.opalj.tac.fpcf.analyses.Statement
import org.opalj.tac.fpcf.properties.IFDSProperty
import org.opalj.tac.fpcf.properties.IFDSPropertyMetaInformation
import org.opalj.tac.fpcf.analyses.AbstractIFDSFact
import org.opalj.fpcf.PropertyKey
import org.opalj.fpcf.PropertyStore
import org.opalj.tac.fpcf.analyses.IFDSAnalysis
import org.opalj.tac.fpcf.analyses.AbstractIFDSNullFact

sealed trait Fact extends AbstractIFDSFact
case object NullFact extends Fact with AbstractIFDSNullFact

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

    override def normalFlow(stmt: Statement, succ: Statement, in: Set[Fact]): Set[Fact] = ???

    override def callFlow(
        stmt:   Statement,
        callee: DeclaredMethod,
        in:     Set[Fact]
    ): Set[Fact] = ???

    override def returnFlow(
        stmt:   Statement,
        callee: DeclaredMethod,
        exit:   Statement,
        succ:   Statement,
        in:     Set[Fact]
    ): Set[Fact] = ???

    override def callToReturnFlow(stmt: Statement, succ: Statement, in: Set[Fact]): Set[Fact] =  ???

    override def nativeCall(statement: Statement, callee: DeclaredMethod, successor: Statement, in: Set[Fact]): Set[Fact] = {
        Set.empty
    }

    override val entryPoints: Map[DeclaredMethod, Fact] = ???

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
