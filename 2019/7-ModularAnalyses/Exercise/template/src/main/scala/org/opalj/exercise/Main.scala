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
import org.opalj.br.fpcf.properties.cg.Callees
import org.opalj.br.fpcf.properties.AllocationFreeMethod
import org.opalj.br.fpcf.properties.AllocationFreeness
import org.opalj.br.fpcf.properties.MethodWithAllocations
import org.opalj.fpcf.EOptionP
import org.opalj.fpcf.ProperPropertyComputationResult
import org.opalj.fpcf.PropertyBounds
import org.opalj.fpcf.PropertyComputationResult
import org.opalj.fpcf.PropertyStore
import org.opalj.fpcf.Result
import org.opalj.fpcf.SomeEPS
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

    // the following two lines should be commented out when running against the JDK
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

    ???
  }

  // the continuation function
  private def c(
      method: DeclaredMethod,
      dependencies: Set[EOptionP[DeclaredMethod, AllocationFreeness]]
  )(
      calleeEPS: SomeEPS
  ): ProperPropertyComputationResult = calleeEPS match {
    case _ => ???
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
