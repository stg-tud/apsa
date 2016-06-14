import org.opalj.br.Method
import org.opalj.br.analyses.Project
import java.net.URL
import scala.collection.JavaConversions._
import org.opalj.br.instructions.Instruction
import heros.solver.IDESolver
import heros.IDETabulationProblem
import heros.EdgeFunction

class DebuggableIDESolver(
    val tabulationProblem: IDETabulationProblem[MInstruction, Fact, Method, ReceiverTypes, OpalICFG])
        extends IDESolver[MInstruction, Fact, Method, ReceiverTypes, OpalICFG](tabulationProblem) {

    def hasPathEdges(method: Method): Boolean = {
        method.body.exists(_.exists { case (pc, instr) ⇒ !jumpFn.lookupByTarget(MInstruction(instr, pc, method)).isEmpty() })
    }

    def printPathEdges(method: Method) = {
        method.body.get.foreach {
            case (pc, instr) ⇒
                val edges = jumpFn.lookupByTarget(MInstruction(instr, pc, method)).map { cell ⇒
                    cell.getRowKey+" -> ["+cell.getColumnKey + ": " + cell.getValue +"]"
                }
                println(f"$pc%2d: ${instr.toString().replaceAll("\n", " ")}%-70.70s \t${edges.mkString(", ")}%s")
        }
    }
    
    def getPathEdgesByTarget(instr: MInstruction) = {
      jumpFn.lookupByTarget(instr).map(cell => (cell.getRowKey, cell.getColumnKey, cell.getValue)).map {
          case (source, target, edgeFn) => PathEdge(instr.pc, instr.i, instr.m, source, target, edgeFn)
      }
    }
    
    override def restoreContextOnReturnedFact(callSite: MInstruction, incomingFact: Fact, returnedFact: Fact) : Fact = {
        (incomingFact, returnedFact) match {
            case (incomingFact: FactWithOperandStack, returnedFact: FactWithOperandStack) =>
                val pops = callSite.i.numberOfPoppedOperands { x ⇒ incomingFact.opStack(x).ctc}
                if(pops > incomingFact.opStack.size)
                    returnedFact
                else {
                    returnedFact.copy(returnedFact.opStack ++ incomingFact.opStack.drop(pops))
                }
                    
            case _ => returnedFact
        }
    }
}

case class PathEdge(
        pc: Int,
        instruction: Instruction,
        method: Method,
        sourceFact: Fact,
        targetFact: Fact,
        edgeFn: EdgeFunction[ReceiverTypes]) {}