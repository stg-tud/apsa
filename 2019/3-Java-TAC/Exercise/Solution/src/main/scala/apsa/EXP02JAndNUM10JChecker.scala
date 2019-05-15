/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package apsa

import java.net.URL

import java.util.concurrent.ConcurrentLinkedQueue

import scala.collection.JavaConverters._

import org.opalj.value.ValueInformation
import org.opalj.collection.immutable.IntTrieSet

import org.opalj.br._
import org.opalj.br.analyses._
import org.opalj.br.instructions._
import org.opalj.br.cfg.CFG
import org.opalj.br.MethodDescriptor.JustTakes
import org.opalj.br.MethodDescriptor.NoArgsAndReturnVoid
import org.opalj.ai.domain.l1.DefaultDomainWithCFGAndDefUse
import org.opalj.tac.ComputeTACAIKey
import org.opalj.tac.TACode
import org.opalj.tac.Stmt
import org.opalj.tac.DUVar
import org.opalj.tac.VirtualFunctionCall
import org.opalj.tac.AssignmentLikeStmt
import org.opalj.tac.New
import org.opalj.tac.Assignment
import org.opalj.tac.NonVirtualMethodCall
import org.opalj.tac.Assignment
import org.opalj.tac.Const
import org.opalj.tac.VirtualMethodCall
import org.opalj.tac.If
import org.opalj.tac.NullExpr
import org.opalj.tac.PutField
import org.opalj.tac.PutStatic
import org.opalj.tac.DefSites
import org.opalj.tac.ReturnValue
import org.opalj.tac.ToTxt

object EXP02JAndNUM10JChecker extends ProjectAnalysisApplication {

  override def description: String = "Finds violations of EXP02-J, NUM10-J and FIO04-J"

  def doAnalyze(p: Project[URL], params: Seq[String], isInterrupted: () ⇒ Boolean): BasicReport = {

    val ch = p.classHierarchy

    p.updateProjectInformationKeyInitializationData(ComputeTACAIKey) { _ ⇒ (m: Method) ⇒
      new DefaultDomainWithCFGAndDefUse(p, m)
    }
    val tacaiProvider = p.get(ComputeTACAIKey)

    val bugs = new ConcurrentLinkedQueue[String]()

    val AutoCloseableType = Type(classOf[AutoCloseable]).asObjectType
    val BigDecimalType = ObjectType("java/math/BigDecimal")
    val EqualsSignature = MethodDescriptor(ObjectType.Object, BooleanType)
    val JustTakesDoubleSignature = JustTakes(DoubleType)

    p.parForeachMethodWithBody(isInterrupted) { mi ⇒
      val m = mi.method
      val taCode = tacaiProvider(m)
      val stmts = taCode.stmts

      // -------------------------------------------------------------------------------------------
      // TAKS 1 - EXP02-J
      /*
        Recall that the compiler _may_ replace the call of equals on an Array with a call
        of equals on java.lang.Object, hence, we need a data-flow analysis.

        The follwing "solution" would only catch bugs where the call goes to the array type:

        p.allMethodsWithBody.foreach { m ⇒
          m.body.get.instructions.collect {
            case i @ INVOKEVIRTUAL(ArrayType(_), "equals", _) ⇒ // report "wrong" usage
          }
        }
       */
      for {
        AssignmentLikeStmt(
          _,
          VirtualFunctionCall(pc, declaringClass, _, "equals", EqualsSignature, receiver, _)
        ) <- stmts
        if declaringClass == ObjectType.Object || declaringClass.isArrayType
        receiverType <- receiver.asVar.value.asReferenceValue.leastUpperType
        if receiverType.isArrayType
      } {
        val line = m.body.get.lineNumber(pc)
        bugs.add(m.toJava(s"pc=$pc/line=$line: unexpected reference comparison of arrays"))
      }

      // -------------------------------------------------------------------------------------------
      // TASK 2 - NUM10-J
      def initializedWithConstant(index: Int): Boolean = {
        if (index < 0)
          return false; // we have the use of a parameter

        stmts(index) match {
          case Assignment(_, _, _: Const) ⇒ true
          case _                          ⇒ false
        }
      }
      for {
        c @ NonVirtualMethodCall(
          pc,
          BigDecimalType,
          _,
          "<init>",
          JustTakesDoubleSignature,
          _,
          params
        ) <- stmts
        // params is either a constant or a variable which is initialized with a variable (depending on the configuration)
        param = params.head // params(0) is the self reference (this)
        if param.isConst || param.asVar.definedBy.forall(initializedWithConstant)
      } {
        val line = m.body.get.lineNumber(pc)
        bugs.add(m.toJava(s"pc=$pc/line=$line: floating-point literal passed to BigDecimal"))
      }
    }

    bugs.asScala.toList.sorted.mkString("\n")
  }
}
