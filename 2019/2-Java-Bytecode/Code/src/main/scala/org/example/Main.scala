package org.example

import java.net.URL
import java.io.File
import java.nio.file.Files

import org.opalj.br.analyses.ProjectAnalysisApplication
import org.opalj.br.analyses.BasicReport
import org.opalj.br.analyses.Project
import org.opalj.ba.{CLASS, CODE, METHOD, METHODS, PUBLIC}
import org.opalj.bc.Assembler
import org.opalj.br.cfg.CFGFactory
import org.opalj.br.instructions.{RETURN, _}
import org.opalj.br.{ClassHierarchy, IntegerType, MethodDescriptor}
import org.opalj.br.ClassHierarchy.PreInitializedClassHierarchy

object Main extends App {

  private def createMethodWithIrreducibleCFG() = {
    CLASS(
      thisType = "IrreducibleCF",
      methods = METHODS(
        METHOD(
          PUBLIC.STATIC,
          "irreducibleM",
          MethodDescriptor.JustTakes(IntegerType).toJVMDescriptor,
          CODE(
            SIPUSH(42),
            ISTORE_0,
            ILOAD_0,
            IFEQ('then),
            'else,
            IINC(0, -1),
            ILOAD_0,
            ILOAD_1,
            IF_ICMPEQ('end),
            'then,
            IINC(0, 2),
            GOTO('else),
            'end,
            RETURN
          )
        )
      )
    )
  }

  val irreducibleCode = createMethodWithIrreducibleCFG()
  val (irreducibleBR, _) = irreducibleCode.toBR
  val (irreducibleDA, _) = irreducibleCode.toDA
  Files.write(new File("IrreducibleCF.class").toPath,Assembler(irreducibleDA))

  val cfg = CFGFactory(irreducibleBR.methods(1), PreInitializedClassHierarchy).get.toDot
  org.opalj.io.writeAndOpen(cfg,"IrreducibleCF",".dot")
}
