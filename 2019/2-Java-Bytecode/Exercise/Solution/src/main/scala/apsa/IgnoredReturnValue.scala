/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package apsa

import java.net.URL

import org.opalj.br._
import org.opalj.br.instructions._
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.ProjectAnalysisApplication
import org.opalj.br.analyses.BasicReport

object IgnoredReturnValue extends ProjectAnalysisApplication {

  override def title: String = "Ignored Method Return Values"

  override def description: String = {
    "Finds method invocations where the return value is immediately popped and therefore ignored."
  }

  override def doAnalyze(p: Project[URL], params: Seq[String], isInterrupted: () ⇒ Boolean): BasicReport = {
    // Parallelization is left as an exercise.

    val methodsWhoseReturnValueIsIgnored =
      p.allMethodsWithBody // let's iterate over all methods which have a body
        .flatMap { m ⇒
          val code = m.body.get
          code
            .collectPair {
              // Let's search for method invocations followed by a pop.
              // Given that the return value of methods which return a long or double
              // value is of computational type category 2, we have to search for
              // some POPInstructions. This approach also matches cases
              // where a POP2 actually pops the returned value and some other value;
              // however, this is not considered a problem, because the return value
              // is popped and that is what we are locking for.
              case (
                MethodInvocationInstruction(declClass, _, name, md),
                _: PopInstruction
                ) if md.returnType != VoidType ⇒
                md.toJava(declClass.toJava, name)
            }
            .map(_.value)
        }
        .toSet.toList.sorted

    methodsWhoseReturnValueIsIgnored.mkString("\n")
  }
}

