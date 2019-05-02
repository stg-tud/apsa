/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package apsa

import java.net.URL

import org.opalj.br._
import org.opalj.br.MethodDescriptor.NoArgsAndReturnVoid
import org.opalj.br.instructions._
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.ProjectAnalysisApplication
import org.opalj.br.analyses.BasicReport

object ThreadStop extends ProjectAnalysisApplication {

  override def description: String = "Finds calls to Thread.stop."

  override def doAnalyze(
    p: Project[URL],
    params: Seq[String],
    isInterrupted: () ⇒ Boolean): BasicReport = {

    val ch = p.classHierarchy
    val ThreadType = ObjectType("java/lang/Thread")

    val performsThreadStopCall: (PC, Instruction) ⇒ Boolean = {
      case (_, MethodInvocationInstruction(t, _, "stop", NoArgsAndReturnVoid)) if (
        ch.isSubtypeOf(
          t,
          ThreadType)) ⇒
        true
      case _ ⇒ false
    }

    val violatingMethods = for {
      m <- p.allMethodsWithBody.par
      c = m.body.get
      if c.exists(performsThreadStopCall)
    } yield {
      m
    }

    violatingMethods.map(_.toJava).toList.sorted.mkString("\n")
  }
}
