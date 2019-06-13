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

  override def description: String = "Finds violations of FIO13-J (logging sensitive information)"

  def doAnalyze(p: Project[URL], params: Seq[String], isInterrupted: () ⇒ Boolean): BasicReport = {

    // compute the call graph and store the *final* result into the property store.
    val callGraph = p.get(RTACallGraphKey)

    val analysesManager = p.get(FPCFAnalysesManagerKey)
    val (propertyStore, _) = analysesManager.runAll(SensitiveLoggingAnalysis)

    "Finished successfully"
  }

}