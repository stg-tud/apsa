/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package apsa

import java.net.URL

import scala.collection.JavaConverters._

import org.opalj.br._
import org.opalj.br.instructions._
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.ProjectAnalysisApplication
import org.opalj.br.analyses.BasicReport

object SecurityChecksInPrivateOrFinalMethods extends ProjectAnalysisApplication {

  override def title: String = "Security Checks in Private or Final Methods"

  override def description: String = {
    "Finds non-private/non-final methods which perform security checks."
  }

  override def doAnalyze(p: Project[URL], params: Seq[String], isInterrupted: () ⇒ Boolean): BasicReport = {
    val violatingMethods = new java.util.concurrent.ConcurrentLinkedQueue[Method]()
    val ch = p.classHierarchy
    val SecurityManagerType = ObjectType("java/lang/SecurityManager")

    val performsSecurityCheck: (PC, Instruction) => Boolean = {
      case (_, MethodInvocationInstruction(sm, _, name, _)) if (
        name != "<init>" &&
        ch.isSubtypeOf(sm, SecurityManagerType)) => true
      case _ => false
    }

    def isEffectivelyFinal(cf: ClassFile): Boolean = {
      cf.isFinal || cf.constructors.forall(_.isPrivate)
    }

    for {
      m <- p.allMethodsWithBody.par
      if !isEffectivelyFinal(m.classFile)
      if m.classFile.thisType != SecurityManagerType
      if !m.isFinal
      if !m.isStatic
      if !m.isPrivate
      c = m.body.get
      if c.exists(performsSecurityCheck)
    } {
      violatingMethods.add(m)
    }

    violatingMethods.asScala.map(_.toJava).toList.sorted.mkString("\n")
  }
}

