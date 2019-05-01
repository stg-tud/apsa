/* BSD 2-Clause License - see OPAL/LICENSE for details. */
package apsa

import java.net.URL

import org.opalj.br._
import org.opalj.br.instructions._
import org.opalj.br.cfg.CFGFactory
import org.opalj.br.analyses.Project
import org.opalj.br.analyses.ProjectAnalysisApplication
import org.opalj.br.analyses.BasicReport

object CFGProperties extends ProjectAnalysisApplication {

  override def title: String = "Basic Properties of CFGs"

  override def description: String = "Computes basic metrics related to CFGs."

  override def doAnalyze(
    p: Project[URL],
    params: Seq[String],
    isInterrupted: () ⇒ Boolean): BasicReport = {

    val frequency = new Array[Int](Char.MaxValue) // maximum size of a method

    var bbInDegreeTotal = 0

    var bbEndsDueToReturn = 0
    var bbEndsDueToPotentialException = 0
    var bbEndsDueToControlTransferInstruction = 0
    var bbEndsBecauseSuccessorIsJumpedTo = 0

    p.allMethodsWithBody.foreach { m ⇒
      val code = m.body.get
      val cfg = CFGFactory(m, p.classHierarchy).get
      cfg.allBBs.foreach { bb ⇒
        val instrCount = bb.countInstructions(code)
        bbInDegreeTotal += bb.predecessors.size
        frequency(instrCount) = frequency(instrCount) + 1

        val bbLastInstruction = code.instructions(bb.endPC)
        if (bbLastInstruction.isReturnInstruction)
          bbEndsDueToReturn += 1
        if (bbLastInstruction.isControlTransferInstruction)
          bbEndsDueToControlTransferInstruction += 1
        if (bb.successors.exists(succBB ⇒ succBB.isCatchNode || succBB.isAbnormalReturnExitNode))
          bbEndsDueToPotentialException += 1
        if (bb.successors.size == 1 && bb.successors.head.predecessors.size > 1)
          bbEndsBecauseSuccessorIsJumpedTo += 1
      }
    }

    var maxCount = 0
    var mostFrequentLength = 0
    var maxBBLength = 0
    var bbCount = 0
    var bbLengthTotal = 0

    frequency.iterator.zipWithIndex.foreach { e ⇒
      val (count, bbLength) = e
      bbCount += count
      bbLengthTotal += (count * bbLength)
      if (count > 0) maxBBLength = bbLength
      if (count >= maxCount) {
        maxCount = count; mostFrequentLength = bbLength
      }
    }

    "Reasons why a basic block ends:\n" +
      "\treturn instruction:     " + bbEndsDueToReturn + "\n" +
      "\tpotential exception:    " + bbEndsDueToPotentialException + "\n" +
      "\tcontrol-transfer:       " + bbEndsDueToControlTransferInstruction + "\n" +
      "\tsuccessor is jumped-to: " + bbEndsBecauseSuccessorIsJumpedTo + "\n" +
      "Average in-degree of basic blocks : " + (bbInDegreeTotal.toDouble / bbCount.toDouble) + "\n" +
      "Most frequent basic-block length  : " + mostFrequentLength + "(" + frequency(
        mostFrequentLength) + "/" + bbCount + ")\n" +
      "Max length of a basic block       : " + maxBBLength + "\n" +
      "Average length of basic blocks    : " + (bbLengthTotal.toDouble / bbCount.toDouble)
  }
}
