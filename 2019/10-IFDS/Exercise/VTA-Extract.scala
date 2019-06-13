trait VTAFact extends SubsumableFact
case object VTANullFact extends VTAFact with SubsumableNullFact

/**
 * A possible run time type of a variable.
 *
 * @param definedBy The variable's definition site.
 * @param t The variable's type.
 * @param upperBound True, if the variable's type could also be every subtype of `t`.
 */
case class VariableType(definedBy: Int, t: ReferenceType, upperBound: Boolean) extends VTAFact {

    /**
     * If this VariableType is an upper bound, it subsumes every subtype.
     */
    override def subsumes(other: AbstractIFDSFact, project: SomeProject): Boolean = {
        if (upperBound) other match {
            case VariableType(definedByOther, tOther, _) if definedBy == definedByOther && t.isObjectType && tOther.isObjectType ⇒
                project.classHierarchy.isSubtypeOf(tOther.asObjectType, t.asObjectType)
            case _ ⇒ false
        }
        else false
    }
}

/**
 * A possible run time type of a call's target.
 *
 * @param line The line of the call.
 * @param t The callee's type.
 * @param upperBound True, if the callee's type could also be every subtype of `t`.
 */
case class CalleeType(line: Int, t: ReferenceType, upperBound: Boolean) extends VTAFact {

    /**
     * If this CalleeType is an upper bound, it subsumes every subtype.
     */
    override def subsumes(other: AbstractIFDSFact, project: SomeProject): Boolean = {
        if (upperBound) other match {
            case CalleeType(lineOther, tOther, _) if line == lineOther && t.isObjectType && tOther.isObjectType ⇒
                tOther.asObjectType.isSubtypeOf(t.asObjectType)(project.classHierarchy)
            case _ ⇒ false
        }
        else false
    }
}

/**
 * A variable type analysis implemented as an IFDS analysis.
 *
 * @param project The analyzed project.
 * @author Mario Trageser
 */
class IFDSBasedVariableTypeAnalysis private (implicit val project: SomeProject)
    extends SubsumingIFDSAnalysis[VTAFact] {

    override val propertyKey: IFDSPropertyMetaInformation[VTAFact] = VTAResult

    /**
     * The analysis starts with all public methods in java.lang or org.opalj.
     */
    override def entryPoints: Seq[(DeclaredMethod, VTAFact)] = {
        p.allProjectClassFiles.filter(classInsideAnalysisContext)
            .flatMap(classFile ⇒ classFile.methods)
            .filter(isEntryPoint).map(method ⇒ declaredMethods(method))
            .flatMap(entryPointsForMethod)
    }

    override protected def nullFact: VTAFact = VTANullFact

    override protected def createPropertyValue(result: Map[Statement, Set[VTAFact]]): IFDSProperty[VTAFact] =
        new VTAResult(result)

    /**
     * If a new object is instantiated and assigned to a variable or array, a new ValueType will be
     * created for the assignment's target.
     * If there is an assignment of a variable or array element, a new VariableType will be
     * created for the assignment's target with the source's type.
     * If there is a field read, a new VariableType will be created with the field's declared type.
     */
    override protected def normalFlow(statement: Statement, successor: Statement,
                                      in: Set[VTAFact]): Set[VTAFact] = statement.stmt.astID match {
        case Assignment.ASTID ⇒
            in ++ newFacts(statement.method, statement.stmt.asAssignment.expr,
                statement.index, in)
        case ArrayStore.ASTID ⇒
            in ++ newFacts(statement.method, statement.stmt.asArrayStore.value, statement.index,
                in).collect {
                case VariableType(_, t, upperBound) if !(t.isArrayType && t.asArrayType.dimensions <= 254) ⇒
                    statement.stmt.asArrayStore.arrayRef.asVar.definedBy
                        .map((arrayDefinedBy: Int) ⇒
                            VariableType(arrayDefinedBy, ArrayType(t), upperBound))
            }.flatten
        case _ ⇒ in
    }

    /**
     * For each variable, which can be passed as an argument to the call, a new VariableType is
     * created for the callee context.
     */
    override protected def callFlow(call: Statement, callee: DeclaredMethod, in: Set[VTAFact],
                                    source: (DeclaredMethod, VTAFact)): Set[VTAFact] = {
        val callObject = asCall(call.stmt)
        val allParams = callObject.receiverOption ++ callObject.params
        in.collect {
            case VariableType(definedBy, t, upperBound) ⇒
                allParams.zipWithIndex.collect {
                    case (parameter, parameterIndex) if parameter.asVar.definedBy.contains(definedBy) ⇒
                        VariableType(
                            paramToIndex(parameterIndex, !callee.definedMethod.isStatic),
                            t, upperBound
                        )
                }
        }.flatten
    }

    /**
     * If the call is an instance call, new CalleeTypes will be created for the call, one for each
     * VariableType, which could be the call's target.
     */
    override protected def callToReturnFlow(call: Statement, successor: Statement,
                                            in:     Set[VTAFact],
                                            source: (DeclaredMethod, VTAFact)): Set[VTAFact] = {
        val calleeDefinitionSites = asCall(call.stmt).receiverOption
            .map(callee ⇒ callee.asVar.definedBy).getOrElse(EmptyIntTrieSet)
        val calleeTypeFacts = in.collect {
            case VariableType(index, t, upperBound) if calleeDefinitionSites.contains(index) ⇒
                CalleeType(call.index, t, upperBound)
        }
        in ++ calleeTypeFacts
    }

    /**
     * If the call returns a value which is assigned to a variable, a new VariableType will be
     * created in the caller context with the returned variable's type.
     */
    override protected def returnFlow(call: Statement, callee: DeclaredMethod, exit: Statement,
                                      successor: Statement, in: Set[VTAFact]): Set[VTAFact] =
        if (exit.stmt.astID == ReturnValue.ASTID && call.stmt.astID == Assignment.ASTID) {
            val returnValue = exit.stmt.asReturnValue.expr.asVar
            in.collect {
                case VariableType(definedBy, t, upperBound) if returnValue.definedBy.contains(definedBy) ⇒
                    VariableType(call.index, t, upperBound)
            }
        } else Set.empty
	
	}
	}