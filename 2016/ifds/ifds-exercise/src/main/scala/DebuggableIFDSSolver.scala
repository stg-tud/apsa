import heros.solver.IFDSSolver
import org.opalj.br.Method
import heros.IFDSTabulationProblem
import org.opalj.br.analyses.Project
import java.net.URL
import scala.collection.JavaConversions._
import org.opalj.br.instructions.Instruction

class DebuggableIFDSSolver(
    val tabulationProblem: IFDSTabulationProblem[MInstruction, Fact, Method, OpalICFG])
        extends IFDSSolver[MInstruction, Fact, Method, OpalICFG](tabulationProblem) {

    def hasPathEdges(method: Method): Boolean = {
        method.body.exists(_.exists { case (pc, instr) ⇒ !jumpFn.lookupByTarget(MInstruction(instr, pc, method)).isEmpty() })
    }

    def printPathEdges(method: Method) = {
        method.body.get.foreach {
            case (pc, instr) ⇒
                val edges = jumpFn.lookupByTarget(MInstruction(instr, pc, method)).map { cell ⇒
                    cell.getRowKey+" -> "+cell.getColumnKey
                }
                println(f"$pc%2d: ${instr.toString().replaceAll("\n", " ")}%-70.70s \t${edges.mkString(", ")}%s")
        }
    }
    
    def getPathEdgesByTarget(instr: MInstruction) = {
      jumpFn.lookupByTarget(instr).map(cell => (cell.getRowKey, cell.getColumnKey)).map {
          case (source, target) => PathEdge(instr.pc, instr.i, instr.m, source, target)
      }
    }
}

case class PathEdge(
        pc: Int,
        instruction: Instruction,
        method: Method,
        sourceFact: Fact,
        targetFact: Fact) {}