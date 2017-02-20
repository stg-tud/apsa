import heros.IFDSTabulationProblem
import org.opalj.br.Method
import heros.FlowFunctions
import heros.FlowFunction
import java.util.{ Set ⇒ JSet }
import java.util.{ Map ⇒ JMap }
import heros.DefaultSeeds
import scala.collection.JavaConverters._
import java.util.HashSet
import org.opalj.br.instructions.INVOKESTATIC
import org.opalj.br.instructions.Instruction
import org.opalj.br.instructions.StoreLocalVariableInstruction
import org.opalj.br.instructions.LoadLocalVariableInstruction
import org.opalj.br.instructions.ReturnInstruction
import org.opalj.br.instructions.InvocationInstruction
import org.opalj.br.instructions.MethodInvocationInstruction
import org.opalj.br.Category1ComputationalTypeCategory
import org.opalj.br.instructions.PUTFIELD
import org.opalj.br.instructions.GETFIELD
import org.opalj.br.instructions.FieldWriteAccess
import org.opalj.br.instructions.FieldReadAccess
import org.opalj.br.instructions.PopInstruction
import org.opalj.br.instructions.POP
import org.opalj.br.instructions.POP2
import org.opalj.br.instructions.NOP
import org.opalj.br.instructions.IINC
import org.opalj.br.instructions.MONITORENTER
import org.opalj.br.instructions.MONITOREXIT
import org.opalj.br.instructions.CHECKCAST
import org.opalj.br.instructions.ReturnValueInstruction
import org.opalj.br.instructions.ArrayStoreInstruction
import org.opalj.br.instructions.ArrayLoadInstruction
import org.opalj.br.instructions.ReturnValueInstruction
import org.opalj.br.instructions.PUTSTATIC
import org.opalj.br.instructions.GETSTATIC
import org.opalj.br.LongType
import org.opalj.br.DoubleType
import org.opalj.br.ComputationalType
import org.opalj.br.Type
import org.opalj.br.instructions.DUP
import org.opalj.br.instructions.SWAP
import org.opalj.br.instructions.DUP_X2
import org.opalj.br.instructions.DUP2_X1
import org.opalj.br.instructions.DUP2_X2
import org.opalj.br.instructions.DUP_X1

class TabulationProblem(
        icfg: OpalICFG,
        seeds: Iterable[MInstruction],
        isSink: INVOKESTATIC ⇒ Boolean) extends IFDSTabulationProblem[MInstruction, Fact, Method, OpalICFG] {

    implicit def ctToStackEntry(tpe: Type): StackEntry = {
        StackEntry(tpe.computationalType.category)
    }

    implicit def ctToStackEntry(ct: ComputationalType): StackEntry = {
        StackEntry(ct.category)
    }

    def recordEdges(): Boolean = false

    def autoAddZero(): Boolean = false

    def computeValues(): Boolean = false

    def flowFunctions() = new FlowFunctions[MInstruction, Fact, Method] {

        def kill() = new HashSet[Fact]

        def gen(facts: Fact*): JSet[Fact] = {
            val res = new HashSet[Fact]
            facts.foreach { fact ⇒ res.add(fact) }
            res
        }

        /**
         * Generic handling of the effect an instruction has on the operand stack. 
         * The fact's operand stack will be updated according to this effect.
         * The returned set will contain the updated fact.
         */
        def handleStackEffect(fact: FactWithOperandStack, instr: Instruction): JSet[Fact] = {
            instr match {
                case LoadLocalVariableInstruction(ct, lvIndex) ⇒ gen(fact.push(StackEntry(ct, lvIndex)))
                case SWAP ⇒
                    val first = fact.opStack.head
                    val second = fact.opStack.tail.head
                    gen(fact.copy(opStack = second :: first :: fact.opStack.drop(2)))

                /* We have to implement specific handling for some StackManagementInstructions (ignore for exercise):
                case DUP 
                case DUP_X1 =>
                case DUP_X2 =>
                case DUP2_X1 =>
                case DUP2_X2 =>
				*/
                
                case _ ⇒
                    val pops = instr.numberOfPoppedOperands { x ⇒ fact.opStack(x).ctc}
                    val pushs = if(instr.isInstanceOf[FieldWriteAccess]) {
                        0
                    } else {
                        instr.numberOfPushedOperands { x ⇒ fact.opStack(x).ctc }
                    }

                    fact match {
                        case OperandStackFact(index, _) if index < pops ⇒ kill()
                        case _ ⇒
                            val factAfterPops = (0 until pops).foldLeft(fact)((f, _) ⇒ f.pop())
                            val factAfterPushs = (0 until pushs).foldLeft(factAfterPops)((f, _) ⇒ f.push(UnknownCTC1Value /*FIXME: ignore for exercise */ ))
                            gen(factAfterPushs)
                    }
            }
        }

        def getNormalFlowFunction(curr: MInstruction, succ: MInstruction) =
            FlowFunction { fact ⇒

                def processNonExceptionalFlow(fact: Fact): JSet[Fact] = {
                    fact match {
                        case Zero => throw new IllegalStateException()
                        
                        case fact @ OperandStackFact(index, stack) => curr.i match {
                        	//TODO: Implement me (consider using different cases for different tainted stack indices)
                            //case StoreLocalVariableInstruction(_, lvIndex)
                            //case PUTFIELD(declaringClass, name, _)
                            //case PUTSTATIC(declaringClass, name, _)
                            //case _: ArrayStoreInstruction
                            //case ArrayLoadInstruction(ct)
                            case instr => handleStackEffect(fact, instr)
                        }
                        
                        case fact @ RegisterFact(lvIndex, stack) => curr.i match {
                        	//TODO: Implement me
                            //case StoreLocalVariableInstruction(_, `lvIndex`)
                            //case LoadLocalVariableInstruction(tpe, `lvIndex`)
                            case instr => handleStackEffect(fact, instr)
                        }
                        
                        case fact @ FieldBasedFact(declaringClass, name, stack) => curr.i match {
                        	//TODO: Implement me
                            //case GETFIELD(`declaringClass`, `name`, tpe)
                            //case GETSTATIC(`declaringClass`, `name`, tpe)
                            case instr => handleStackEffect(fact, instr)
                        }
                    }
                }

                val isExceptionHandler = curr.m.body.get.exceptionHandlers.exists { x ⇒ x.handlerPC == curr.pc }
                if (isExceptionHandler) {
                    // an exception has been thrown and is handled at the current instruction:
                    // - whatever was on the operand stack has been removed
                    // - the thrown exception was placed on the operand stack
                    fact match {
                        case _: OperandStackFact ⇒ kill()
                        case rf: RegisterFact    ⇒ processNonExceptionalFlow(rf.copy(opStack = List(UnknownCTC1Value)))
                        case fbf: FieldBasedFact ⇒ processNonExceptionalFlow(fbf.copy(opStack = List(UnknownCTC1Value)))
                    }
                } else
                    processNonExceptionalFlow(fact)
            }

        def getCallFlowFunction(callSite: MInstruction, calledMethod: Method) =
            FlowFunction { fact ⇒
                callSite.i match {
                    case i: INVOKESTATIC if isSink(i) ⇒ fact match {
                        case f @ OperandStackFact(0, _) ⇒
                            println("reached sink"); kill()
                        case _ ⇒ kill()
                    }

                    case _ ⇒ fact match {
                        //TODO: Implement me; Hint: use operandStackIndexToRegisterIndex(calledMethod)
                        case _               ⇒ kill()
                    }
                }
            }

        /**
         * Maps entries of the operand stack on the caller side to register indices on the callee side.
         * Example:
         * For the call receiver.foo(p1, p2) the top-most values on the operand stack represent:
         * p2, p1, receiver
         * Assuming these have computational types of category 1 they will be mapped to the registers:
         * p2=register 2, p1=register 1, receiver=register 0
         * Accordingly, this function returns List(2,1,0) for the example.
         * Assume p1 has a computational type of category 2, then the mapping to registers is:
         * p2=register 3, p1=register 1, receiver=register 0
         * Accordingly, this function returns List(3,1,0).
         */
        def operandStackIndexToRegisterIndex(calledMethod: Method): Seq[Int] = {
            val params = calledMethod.parameterTypes.map[Int, Seq[Int]](x ⇒ x.computationalType.operandSize).toList
            def f(sum: Int, list: List[Int], result: List[Int]): List[Int] = {
                if (list.isEmpty)
                    result
                else
                    f(sum + list.head, list.tail, sum :: result)
            }
            f(0, if (calledMethod.isStatic) params else 1 :: params, Nil)
        }

        def getCallToReturnFlowFunction(callSite: MInstruction, returnSite: MInstruction) =
            FlowFunction { fact ⇒
                fact match {
                    case Zero                   ⇒ gen(OperandStackFact(0, List(UnknownCTC1Value)))
                    //TODO: Implement me
                    case fact: FactWithOperandStack => handleStackEffect(fact, callSite.i)
                }
            }

        def getReturnFlowFunction(callSite: MInstruction, calleeMethod: Method, exitStmt: MInstruction, returnSite: MInstruction) =
            FlowFunction { fact ⇒
                //TODO: Implement me
                kill()
            }
    }

    def followReturnsPastSeeds(): Boolean = true

    def initialSeeds(): JMap[MInstruction, JSet[Fact]] = DefaultSeeds.make(seeds.asJava, zeroValue());

    def interproceduralCFG(): OpalICFG = icfg

    def numThreads(): Int = Runtime.getRuntime().availableProcessors()

    def zeroValue(): Fact = Zero
}

object FlowFunction {

    def apply(f: Fact ⇒ JSet[Fact]): FlowFunction[Fact] = {
        new FlowFunction[Fact] {
            def computeTargets(fact: Fact): JSet[Fact] = f(fact)
        }
    }

}
