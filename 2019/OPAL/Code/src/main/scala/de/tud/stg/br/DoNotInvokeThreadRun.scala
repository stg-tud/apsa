/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package de.tud.stg

package br

import java.net.URL
import scala.collection.JavaConverters._

import org.opalj.util.PerformanceEvaluation
import org.opalj.br._
import org.opalj.br.analyses._
import org.opalj.br.instructions._

object DoNotInvokeThreadRun extends ProjectAnalysisApplication {

  override def description: String = {
    "Finds Thread.run calls (See The CERT Oracle Secure Coding Standard for Java - Rule TH100-J - for details.)."
  }

  final val ThreadType = ObjectType("java/lang/Thread")

  def doAnalyze(p: Project[URL], params: Seq[String], isInterrupted: () ⇒ Boolean): BasicReport = {
    val ch = p.classHierarchy

    def violationsUsingForComprehension(): String = {
      val violations =
        for {
          m <- p.allMethodsWithBody.par
          c = m.body.get
          INVOKEVIRTUAL(declClass, "run", MethodDescriptor.NoArgsAndReturnVoid) <- c.iterator
          if ch.isSubtypeOf(declClass, ThreadType)
        } yield {
          m.toJava(s"call to run method of subtype of Thread: " + declClass.toJava)
        }
      violations.mkString("\n")
    }

    def violationsUsingHigherOrderFunction(): String = {
      val violations = new java.util.concurrent.ConcurrentLinkedQueue[String]()
      p.parForeachMethodWithBody(isInterrupted) { mi ⇒
        val m = mi.method
        val violatingCalls = m.body.get.collect {
          case INVOKEVIRTUAL(declClass, "run", MethodDescriptor.NoArgsAndReturnVoid)
              if (ch.isSubtypeOf(declClass, ThreadType)) ⇒
            declClass
        }
        violatingCalls.foreach {
          case PCAndAnyRef(pc, declClass) ⇒
            val msg = m.toJava(s"$pc: call to run method of subtype of Thread: " + declClass.toJava)
            violations.add(msg)
        }
      }
      violations.asScala.mkString("\n")
    }

    val r2 = PerformanceEvaluation.time(3, 15, 8, violationsUsingHigherOrderFunction, true) {
      (ns, nss) ⇒
        val considered = nss.map(_.toSeconds).mkString("[", ", ", "]")
        println(s"violationsUsingHigherOrderFunction took ${ns.toSeconds} $considered")
    }

    val r1 = PerformanceEvaluation.time(3, 15, 8, violationsUsingForComprehension, true) {
      (ns, nss) ⇒
        val considered = nss.map(_.toSeconds).mkString("[", ", ", "]")
        println(s"violationsUsingForComprehension took ${ns.toSeconds} $considered")
    }

    // on a 6-core CPU...:
    //  - the ".par" version is roughly 4 times faster than the seq version
    //  - the "parForeachMethodWithBody" version is roughly 10 times faster than the seq version

    if (r1.lines.size != r2.lines.size) throw new UnknownError(s"$r1 != $r2")
    r1
  }
}
