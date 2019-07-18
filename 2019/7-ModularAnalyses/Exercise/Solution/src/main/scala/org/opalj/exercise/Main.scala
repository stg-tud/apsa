package org.opalj.exercise

import java.net.URL

import org.opalj.br.DeclaredMethod
import org.opalj.br.analyses.BasicReport
import org.opalj.br.analyses.DeclaredMethodsKey
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.ProjectAnalysisApplication
import org.opalj.br.analyses.SomeProject
import org.opalj.br.fpcf.BasicFPCFEagerAnalysisScheduler
import org.opalj.br.fpcf.FPCFAnalysis
import org.opalj.br.fpcf.FPCFAnalysesManagerKey
import org.opalj.br.fpcf.cg.properties.Callees
import org.opalj.br.fpcf.properties.AllocationFreeMethod
import org.opalj.br.fpcf.properties.AllocationFreeness
import org.opalj.br.fpcf.properties.MethodWithAllocations
import org.opalj.fpcf.EOptionP
import org.opalj.fpcf.EPK
import org.opalj.fpcf.InterimResult
import org.opalj.fpcf.InterimUBP
import org.opalj.fpcf.UBP
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyComputationResult
import org.opalj.fpcf.PropertyStore
import org.opalj.fpcf.Result
import org.opalj.fpcf.SomeEPS
import org.opalj.tac.Assignment
import org.opalj.tac.ExprStmt
import org.opalj.tac.New
import org.opalj.tac.NewArray
import org.opalj.tac.cg.RTACallGraphKey
import org.opalj.tac.fpcf.properties.TACAI

object Main extends ProjectAnalysisApplication {

  override def description: String = "Counts the number of methods with and without allocations"

  def doAnalyze(p: Project[URL], params: Seq[String], isInterrupted: () ⇒ Boolean): BasicReport = {

    // compute the call graph and store the *final* result into the property store.
    val callGraph = p.get(RTACallGraphKey)

    val analysesManager = p.get(FPCFAnalysesManagerKey)
    val (propertyStore, _) = analysesManager.runAll(EagerAllocationFreenessAnalysis)

    val allocationFreenessResults = propertyStore.entities(AllocationFreeness.key).toList
    val (allocationFreeMethods, methodsWithAllocations) =  allocationFreenessResults.partition(_.asFinal.p == AllocationFreeMethod)

    allocationFreeMethods.map(_.e).mkString("allocation free: ", "\nallocation free: ", "\n")+
    methodsWithAllocations.map(_.e).mkString("with allocations: ", "\nwith allocations: ", "\n")+
    s"# methods: ${allocationFreenessResults.size}\n"+
     s"# allocation free methods: ${allocationFreeMethods.size}\n"+
     s"# methods with allocations: ${methodsWithAllocations.size}\n"
  }

}

class AllocationFreenessAnalysis private[exercise] (final val project: SomeProject)
    extends FPCFAnalysis {

  private implicit val declaredMethods = project.get(DeclaredMethodsKey)

  def analyze(method: DeclaredMethod): PropertyComputationResult = {
    if (!method.hasSingleDefinedMethod)
      return Result(method, MethodWithAllocations);

    if (method.definedMethod.isNative)
      return Result(method, MethodWithAllocations);

    val tacProperty = ps(method.definedMethod, TACAI.key)

    // While computing the call graph, the three-address code gets also computed for all 
    // non-native methods. Hence, it will be available!
    assert(tacProperty.isFinal && tacProperty.asFinal.p.tac.isDefined)
    val tac = tacProperty.asFinal.p.tac.get
    // For every statement we check whether it performs an allocation (New or NewArray).
    // Allocations within methods called by the current one will be handled afterwards.
    for (stmt <- tac.stmts) {
      stmt match {
        case Assignment(_, _, _: New | _: NewArray[_]) ⇒
          return Result(method, MethodWithAllocations);

        case ExprStmt(_, _: New | _: NewArray[_]) ⇒
          return Result(method, MethodWithAllocations);

        case _ ⇒
      }
    }

    // When we reach this point, there are no allocations within this method.
    // However, methods that are called by the current one might still have allocations.
    // So lets check them:

    // retrieve the callees of the method.
    val calleesProperty = ps(method, Callees.key)

    // as the call graph gets computed beforehand, it must be final
    assert(calleesProperty.isFinal)

    // we will need to keep track of entity/property pairs that are not yet final, 
    // i.e. we must listen for updates.
    var dependencies = Set.empty[EOptionP[DeclaredMethod, AllocationFreeness]]

    // iterate over all call targets and check whether these methods have allocations
    for {
      (_, callTargets) <- calleesProperty.asFinal.p.callSites()
      callTarget <- callTargets
      if callTarget != method
    } {
      ps(callTarget, AllocationFreeness.key) match {
        case UBP(MethodWithAllocations) ⇒
          return Result(method, MethodWithAllocations);

        case dependency @ InterimUBP(AllocationFreeMethod) ⇒
          // the result is not final yet
          dependencies += dependency

        case dependency: EPK[DeclaredMethod, AllocationFreeness] => 
          // there is no result yet
          dependencies += dependency

        case _ ⇒ // this is the call target has definitivly no allocations
      }
    }

    if (dependencies.isEmpty) {
      // if there are no dependencies and we did not return before, 
      // the method has no allocations
      Result(method, AllocationFreeMethod)
    } else {
      // otherwise, we register the continuation function for our dependencies
      InterimResult(
        method,
        MethodWithAllocations,
        AllocationFreeMethod,
        dependencies,
        c(method, dependencies)
      )
    }
  }

  // the continuation function
  private def c(
      method: DeclaredMethod,
      dependencies: Set[EOptionP[DeclaredMethod, AllocationFreeness]]
  )(
      calleeEPS: SomeEPS
  ): ProperPropertyComputationResult = calleeEPS match {
    case UBP(MethodWithAllocations) ⇒
      Result(method, MethodWithAllocations)

    case InterimUBP(AllocationFreeMethod) ⇒
      // we need to update the state of the dependency
      val updatedDependencies = dependencies.filterNot(_.e == calleeEPS.e) + 
        calleeEPS.asInstanceOf[EPS[DeclaredMethod, AllocationFreeness]]
      InterimResult(
        method,
        MethodWithAllocations,
        AllocationFreeMethod,
        updatedDependencies,
        c(method, updatedDependencies)
      )

    case _ ⇒
      // this is the call target has definitivly no allocations.
      // therefore, we can remove the call target from our dependencies.
      val updatedDependencies = dependencies.filterNot(_.e == calleeEPS.e)

      if (updatedDependencies.isEmpty) {
        Result(method, AllocationFreeMethod)
      } else {
        InterimResult(
          method,
          MethodWithAllocations,
          AllocationFreeMethod,
          updatedDependencies,
          c(method, updatedDependencies)
        )
      }
  }

}

object EagerAllocationFreenessAnalysis extends BasicFPCFEagerAnalysisScheduler {
  override def uses: Set[PropertyBounds] = 
    PropertyBounds.finalPs(Callees, TACAI) + PropertyBounds.ub(AllocationFreeness) // todo ub => lb
  
  override def derivesEagerly: Set[PropertyBounds] = Set(PropertyBounds.ub(AllocationFreeness))

  override def derivesCollaboratively: Set[PropertyBounds] = Set.empty

  override def start(project: SomeProject, propertyStore: PropertyStore, i: Null): FPCFAnalysis = {

    // retrieve the cached call graph
    val callGraph = project.get(RTACallGraphKey)

    val analysis = new AllocationFreenessAnalysis(project)

    // compute the allocation freeness property for all reachable methods
    propertyStore.scheduleEagerComputationsForEntities(callGraph.reachableMethods())(
      analysis.analyze
    )

    analysis
  }
}
