import org.scalatest.Matchers
import org.scalatest.FunSpec
import org.scalatest.junit.JUnitRunner
import org.opalj.br.analyses.Project
import java.io.File

import org.opalj.ai.analyses.cg.ComputedCallGraph
import org.opalj.ai.analyses.cg.VTACallGraphKey
import heros.InterproceduralCFG
import org.opalj.br.instructions.Instruction
import org.opalj.br.Method
import java.util.{ List ⇒ JList }
import java.util.{ Collection ⇒ JCollection }
import java.util.{ Set ⇒ JSet }
import org.opalj.ai.analyses.cg.CallGraph
import java.util.Collections
import org.opalj.br.instructions.INVOKESPECIAL
import scala.collection.JavaConverters._
import org.opalj.br.cfg.CFG
import org.opalj.br.cfg.CFGFactory
import java.util.concurrent.ConcurrentHashMap
import org.opalj.br.cfg.CatchNode
import org.opalj.br.cfg.BasicBlock
import org.opalj.br.instructions.MethodInvocationInstruction
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.HashSet
import heros.solver.IDESolver
import heros.solver.IFDSSolver
import heros.IFDSTabulationProblem
import org.opalj.br.instructions.INVOKEVIRTUAL
import org.opalj.br.instructions.INVOKESTATIC
import scala.collection.JavaConversions._
import org.opalj.br.ClassFile
import Function.tupled
import org.opalj.br.instructions.StoreLocalVariableInstruction
import org.opalj.br.instructions.ArrayStoreInstruction
import org.opalj.br.instructions.ArrayLoadInstruction
import org.opalj.br.instructions.GETFIELD
import org.opalj.br.instructions.LoadLocalVariableInstruction
import org.opalj.br.instructions.ReturnValueInstruction
import org.opalj.br.instructions.GETSTATIC
import org.opalj.br.ObjectType
import heros.edgefunc.EdgeIdentity

class AnalysisTests extends FunSpec with Matchers {

    def isSource(i: INVOKESTATIC) = i.name == "source" && i.declaringClass.fqn == "util/SourceAndSink"

    def isSink(i: INVOKESTATIC) = i.name == "sink" && i.declaringClass.fqn == "util/SourceAndSink"

    //Enable logging of IDESolver
    System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");

    val theProject = Project(new File("../testcases/target/scala-2.10/test-classes"))

    val ComputedCallGraph(callGraph, /*we don't care about unresolved methods etc. */ _, _) =
        theProject.get(VTACallGraphKey)
    val icfg = new OpalICFG(callGraph)

    def runAnalysis(method: Method): DebuggableIDESolver = {
        val seeds = method.body.get.collectWithIndex {
            case (pc, i: INVOKESTATIC) if isSource(i) ⇒ MInstruction(i, pc, method)
        }
        val solver = new DebuggableIDESolver(new TabulationProblem(theProject, icfg, seeds, isSink))
        solver.solve()
        printPathEdges(solver)
        solver
    }

    def printPathEdges(solver: DebuggableIDESolver): Unit = {
        for {
            classFile ← theProject.allClassFiles
            method ← classFile.methods
            if solver.hasPathEdges(method)
        } {
            println(method.toJava(classFile))
            solver.printPathEdges(method)
            println("--------")
        }
    }

    def testClass(className: String)(fun: ClassFile ⇒ Unit) {
        describe("For class "+className) {
            val classFile = theProject.allClassFiles.find { classFile ⇒ classFile.thisType.fqn == className }.get
            fun(classFile)
        }
    }

    def testMethod(classFile: ClassFile)(methodName: String)(fun: (Method, ⇒ Iterable[PathEdge]) ⇒ Unit) {
        describe("the analysis starts in method "+methodName) {
            val method = classFile.methods.find { method ⇒ method.name == methodName }.get
            lazy val edges = {
                val solver = runAnalysis(method)
                theProject.allMethodsWithBody.filter(_.body.isDefined).flatMap { method ⇒
                    method.body.get.associateWithIndex().flatMap {
                        case (pc, instr) ⇒ solver.getPathEdgesByTarget(MInstruction(instr, pc, method))
                    }
                }
            }
            fun(method, edges)
        }
    }

    testClass("tests/Assignments") { classFile ⇒
        testMethod(classFile)("foo") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should taint the upmost value on the operand stack when storing to register 0") {
                exactly(1, edges) should matchPattern { case PathEdge(_, StoreLocalVariableInstruction(_, 0), _, Zero, OperandStackFact(0, _), _) ⇒ }
            }
            it("should have register 0 tainted at the sink") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, RegisterFact(0, _), _) if isSink(i) ⇒ }
            }
            it("should have the upmost value on the operand stack tainted at the sink") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, OperandStackFact(0, _), _) if isSink(i) ⇒ }
            }
        }
    }

    testClass("tests/Arrays") { classFile ⇒
        testMethod(classFile)("foo") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should taint the upmost value on the operand stack when storing to the array") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: ArrayStoreInstruction, _, Zero, OperandStackFact(0, _), _) ⇒ }
            }
            it("should have the array tainted when loading from the array (assuming the analysis is insensitive to array indices)") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: ArrayLoadInstruction, _, Zero, RegisterFact(1, _), _) ⇒ }
            }
            it("should have the upmost value on the operand stack tainted at the sink") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, OperandStackFact(0, _), _) if isSink(i) ⇒ }
            }
        }

        testMethod(classFile)("bar") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should taint the upmost value on the operand stack when storing to the array") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: ArrayStoreInstruction, _, Zero, OperandStackFact(0, _), _) ⇒ }
            }
            it("should have the array tainted when loading from the array") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: ArrayLoadInstruction, _, Zero, RegisterFact(1, _), _) ⇒ }
            }
            it("should have the upmost value on the operand stack tainted at the sink") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, OperandStackFact(0, _), _) if isSink(i) ⇒ }
            }
        }
    }

    testClass("tests/InstanceField") { classFile ⇒
        testMethod(classFile)("foo") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(2, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should have field f tainted (field based) when getting its value (two times)") {
                exactly(2, edges) should matchPattern { case PathEdge(_, GETFIELD(_, "f", _), _, Zero, FieldBasedFact(_, "f", _), _) ⇒ }
            }
            it("should have the upmost value on the operand stack tainted at the sink") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, OperandStackFact(0, _), _) if isSink(i) ⇒ }
            }
        }

        testMethod(classFile)("bar") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should have field f tainted (field based) when getting its value") {
                exactly(1, edges) should matchPattern { case PathEdge(_, GETFIELD(_, "f", _), _, Zero, FieldBasedFact(_, "f", _), _) ⇒ }
            }
            it("should have the upmost value on the operand stack tainted at the sink") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, OperandStackFact(0, _), _) if isSink(i) ⇒ }
            }
        }
    }

    testClass("tests/InterproceduralInstanceBasedEdges") { classFile ⇒
        testMethod(classFile)("foo") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should have a self-loop edge at the start of method bar") {
                exactly(1, edges) should matchPattern { case PathEdge(0, i: LoadLocalVariableInstruction, Method(_, "bar", _), RegisterFact(1, _), RegisterFact(1, _), _) ⇒ }
            }
            it("should return a tainted value from method bar") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: ReturnValueInstruction, Method(_, "bar", _), RegisterFact(1, _), OperandStackFact(0, _), _) ⇒ }
                exactly(1, edges) should matchPattern { case PathEdge(_, StoreLocalVariableInstruction(_, 2), Method(_, "foo", _), Zero, OperandStackFact(0, _), _) ⇒ }
            }
            it("should have the upmost value on the operand stack tainted at the sink") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, OperandStackFact(0, _), _) if isSink(i) ⇒ }
            }
        }
    }

    testClass("tests/InterproceduralStaticEdges") { classFile ⇒
        testMethod(classFile)("foo") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should have a self-loop edge at the start of method bar") {
                exactly(1, edges) should matchPattern { case PathEdge(0, i: LoadLocalVariableInstruction, Method(_, "bar", _), RegisterFact(0, _), RegisterFact(0, _), _) ⇒ }
            }
            it("should return a tainted value from method bar") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: ReturnValueInstruction, Method(_, "bar", _), RegisterFact(0, _), OperandStackFact(0, _), _) ⇒ }
                exactly(1, edges) should matchPattern { case PathEdge(_, StoreLocalVariableInstruction(_, 1), Method(_, "foo", _), Zero, OperandStackFact(0, _), _) ⇒ }
            }
            it("should have the upmost value on the operand stack tainted at the sink") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, OperandStackFact(0, _), _) if isSink(i) ⇒ }
            }
        }
    }

    testClass("tests/MultipleInstanceFields") { classFile ⇒
        testMethod(classFile)("foo") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should have field f tainted (field based) when getting its value") {
                exactly(1, edges) should matchPattern { case PathEdge(_, GETFIELD(_, "f", _), _, Zero, FieldBasedFact(_, "f", _), _) ⇒ }
            }
            it("should have the upmost value on the operand stack tainted at the sink") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, OperandStackFact(0, _), _) if isSink(i) ⇒ }
            }
        }
    }

    testClass("tests/StaticField") { classFile ⇒
        testMethod(classFile)("foo") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should have field f tainted (field based) when invoking bar") {
                exactly(1, edges) should matchPattern { case PathEdge(_, INVOKESTATIC(_, _, "bar", _), _, Zero, FieldBasedFact(_, "f", _), _) ⇒ }
            }
            it("should have a self-loop edge at the start of bar") {
                exactly(1, edges) should matchPattern { case PathEdge(0, _, Method(_, "bar", _), FieldBasedFact(_, "f", _), FieldBasedFact(_, "f", _), _) ⇒ }
            }
            it("should have field f tainted (field based) when getting its value") {
                exactly(1, edges) should matchPattern { case PathEdge(_, GETSTATIC(_, "f", _), _, FieldBasedFact(_, "f", _), FieldBasedFact(_, "f", _), _) ⇒ }
            }
            it("should have the upmost value on the operand stack tainted at the sink") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, _, FieldBasedFact(_, "f", _), _) if isSink(i) ⇒ }
            }
        }
    }

    testClass("tests/Category2Values") { classFile ⇒
        testMethod(classFile)("foo") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should have a self-loop edge at the start of bar") {
                exactly(1, edges) should matchPattern { case PathEdge(0, _, Method(_, "bar", _), RegisterFact(2, _), RegisterFact(2, _), _) ⇒ }
            }
            it("should have the upmost value on the operand stack tainted at the sink") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, Method(_, "bar", _), RegisterFact(2, _), OperandStackFact(0, _), _) if isSink(i) ⇒ }
            }
        }
    }

    testClass("tests/Sanitization") { classFile ⇒
        testMethod(classFile)("foo") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should have killed the taint after sanitization") {
                no(edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, _, _, _) if isSink(i) ⇒ }
            }
        }
    }
        
    testClass("tests/CorrelatedCalls") { classFile =>
         testMethod(classFile)("main") { (method, edges) ⇒
            it("should have a self-loop edge as initial seed") {
                exactly(1, edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, Zero, Zero, _) if isSource(i) ⇒ }
            }
            it("should have the parameter tainted of B.foo and C.foo") {
            	exactly(2, edges) should matchPattern { case PathEdge(_, _: ReturnValueInstruction, Method(_, "foo", _), RegisterFact(1, _), RegisterFact(1, _), id: EdgeIdentity[ReceiverTypes]) ⇒ }
            }
            it("should have a edge function mapping register 0 to type B after returning from invoking foo") {
            	exactly(1, edges) should matchPattern { case PathEdge(11, i: StoreLocalVariableInstruction, _, Zero, OperandStackFact(0, _), MapSingleRegister(0, ObjectType("tests/CorrelatedCalls$B"))) ⇒ }
            }
            it("should not find a flow to sink") {
                no(edges) should matchPattern { case PathEdge(_, i: INVOKESTATIC, _, _, _, _) if isSink(i) ⇒ }
            }
        }
    }
}