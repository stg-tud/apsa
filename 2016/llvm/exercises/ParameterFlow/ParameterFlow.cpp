#include "llvm/Pass.h"
#include "llvm/IR/Instructions.h"
#include "llvm/IR/Module.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/InstVisitor.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/ADT/DenseSet.h"
#include <set>
#include "llvm/IR/CFG.h"

#define LOOPHANDLER_NONE 0
#define LOOPHANDLER_PROCESS_ONCE 1
#define LOOPHANDLER_INPUT_DIFFERS 2
#define LOOPHANDLER_OUTPUT_DIFFERS 3

#define LOOPHANDLER LOOPHANDLER_NONE  

using namespace llvm;

namespace {

  /* Naive parameter flow */
  struct FlowIV : public InstVisitor<FlowIV> {
 
    DenseSet<Value*> trackedValues;

    void addTrackedValue(Value &v) {
      addTrackedValue(&v);
    }

    void addTrackedValue(Value *v) {
      //errs() << "Now tracking ";
      //errs().write_escaped(v->getName());
      //errs() << "\n";
      trackedValues.insert(v);
    }

    DenseSet<Value*> getCurrentlyTrackedValues() {
      return trackedValues;
    }

    bool isTracked(Value &v) {
      return isTracked(&v);
    }

    bool isTracked(Value *v) {
      return (trackedValues.end() != trackedValues.find(v));
    }

    void visitLoadInst(LoadInst &I) {
      if (isTracked(I.getPointerOperand())) {
        errs() << "Loading tracked pointer ";
        errs().write_escaped(I.getPointerOperand()->getName());
        errs() << " into value ";
        errs().write_escaped(I.getName());
        errs() << "\n";

        addTrackedValue(I);
      }
    }

    void visitStoreInst(StoreInst &I) {
      if (isTracked(I.getValueOperand())) {
        errs() << "Storing tracked value ";
        errs().write_escaped(I.getValueOperand()->getName());
        errs() << " in pointer ";
        errs().write_escaped(I.getPointerOperand()->getName());
        errs() << "\n";

        addTrackedValue(I.getPointerOperand());
      } 
    }

    void visitBinaryOperator(BinaryOperator &I) {
      Value *op1 = I.getOperand(0);
      Value *op2 = I.getOperand(1);

      if (isTracked(op1) || isTracked(op2)) {
        errs() << "Storing the result of binary operator (" << I.getOpcodeName() << ") in value ";
        errs().write_escaped(I.getName());
        errs() << "\n";

        addTrackedValue(I);
      }
    }

    void visitUnaryInstruction(UnaryInstruction &I) {
      Value *op1 = I.getOperand(0);

      if (isTracked(op1)) {
        errs() << "unary: ";
        errs() << I.getOpcodeName();
        errs() << "\n";

        addTrackedValue(I);
      }   
    }

    void visitReturnInst(ReturnInst &I) {
      Value *op1 = I.getOperand(0);
      if (isTracked(op1)) {
        errs() << "Returning tracked value ";
        errs().write_escaped(op1->getName());
        errs() << "\n";
      }
    }

    void visitCallInst(CallInst &I) {
      for(Use &v : I.arg_operands()) {
        if (isTracked(v.get())) {
          errs() << "Call to ";
          errs().write_escaped(I.getCalledFunction()->getName());
          errs() << " uses at least one tracked value, tracking result in ";
          errs().write_escaped(I.getName());
          errs() << "\n";

          addTrackedValue(I);
        }
      }
    }

  };



	struct ParameterFlow : public FunctionPass {
		static char ID;
		ParameterFlow() : FunctionPass(ID) {}

		bool runOnFunction(Function& f) override {

      // run for each argument of the function
      for(Argument &a : f.getArgumentList()) {
        errs() << "Tracing argument ";
        errs().write_escaped(a.getName());
        errs() << " of function ";
        errs().write_escaped(f.getName());
        errs() << "\n";

        FlowIV flowVisitor;
        flowVisitor.addTrackedValue(a);
        flowVisitor.visit(f);

      }
			return false;
		}
	};

  /* Flow-sensitive parameter flow */
  struct FlowSensitivePV : public FunctionPass {
    static char ID;
    FlowSensitivePV() : FunctionPass(ID) {}

    bool runOnFunction(Function& f) override {

      // run for each argument of the function
      for(Argument &a : f.getArgumentList()) {
        errs() << "Tracing argument ";
        errs().write_escaped(a.getName());
        errs() << " of function ";
        errs().write_escaped(f.getName());
        errs() << "\n";
        
        // initialize worklist as a pair of basic blocks with their incoming facts
        std::vector<std::pair<BasicBlock*, DenseSet<Value*>>> worklist;

        // prepare initial seed for worklist
        // find basic block w/o predecessors
        // flag the currently observed parameter value as tainted
        for (BasicBlock &bb : f) {
          if (!hasPredecessor(&bb)) {
            DenseSet<Value*> incomingValues;
            incomingValues.insert(&a);
            worklist.push_back(std::make_pair(&bb, incomingValues));
          }
        }

        #if LOOPHANDLER == LOOPHANDLER_PROCESS_ONCE
        DenseSet<BasicBlock*> processed;
        #endif
        #if LOOPHANDLER == LOOPHANDLER_INPUT_DIFFERS
        DenseMap<BasicBlock*, DenseSet<Value*>> knownInputs;
        #endif
        #if LOOPHANDLER == LOOPHANDLER_OUTPUT_DIFFERS
        DenseMap<BasicBlock*, DenseSet<Value*>> previousOutputs;
        #endif

        // iterate over worklist until empty
        while(!worklist.empty()) {
          // get the last pair in the worklist and pop it
          std::pair<BasicBlock*, DenseSet<Value*>> current = worklist.back();
          worklist.pop_back();

          #if LOOPHANDLER == LOOPHANDLER_INPUT_DIFFERS 
          DenseSet<Value*> processedBeforeWith = knownInputs.lookup(current.first);
          if (processedBeforeWith.size() > 0 && valueSetsAreEqual(current.second, processedBeforeWith)) 
            // we already know these input values
            continue;
          #endif

          // visit the basic block using our visitor
          // assume the incoming values of the previous block
          FlowIV flowVisitor;
          for (Value *v : current.second) 
            flowVisitor.addTrackedValue(v);
          flowVisitor.visit(current.first);
          DenseSet<Value*> outgoingTracked = flowVisitor.getCurrentlyTrackedValues();

          #if LOOPHANDLER == LOOPHANDLER_PROCESS_ONCE
          processed.insert(current.first);
          #endif
          #if LOOPHANDLER == LOOPHANDLER_INPUT_DIFFERS
          if (knownInputs.count(current.first) == 1) 
            knownInputs.erase(current.first);
          knownInputs.insert(std::make_pair(current.first, current.second));
          #endif


          #if LOOPHANDLER == LOOPHANDLER_OUTPUT_DIFFERS 
          if (previousOutputs.count(current.first) > 0) {
            DenseSet<Value*> previousOutput = previousOutputs.lookup(current.first);
            if (previousOutput.size() > 0 && valueSetsAreEqual(outgoingTracked, previousOutput)) 
              continue;
            previousOutputs.erase(current.first);
          }

          previousOutputs.insert(std::make_pair(current.first, outgoingTracked));
          #endif

          // add successor blocks to the work list and use the outgoing values of this block as their incoming values
          TerminatorInst *blockTi = current.first->getTerminator();
          if (blockTi) {
            for(BasicBlock *succ : blockTi->successors()) {
              #if LOOPHANDLER == LOOPHANDLER_PROCESS_ONCE
              if(processed.find(succ) == processed.end())
              #endif
                worklist.push_back(std::make_pair(succ, outgoingTracked));
            }
          }

          
        }

      }
      return false;
    }

    bool hasPredecessor(BasicBlock* bb) {
      if (pred_begin(bb) != pred_end(bb)) return true;
      return false;
    }

    bool valueSetsAreEqual(DenseSet<Value*> first, DenseSet<Value*> second) {
      if (first.size() != second.size()) return false;
      for (Value *v : first) 
        if (second.count(v) == 0)
          return false;
      for (Value *v : second) 
        if (first.count(v) == 0)
          return false;
      return true;
    }
  };


}

char ParameterFlow::ID = 0;
static RegisterPass<ParameterFlow> A("ParameterFlow", "Tracks the flow of parameters", false, false);

char FlowSensitivePV::ID = 1;
static RegisterPass<FlowSensitivePV> B("FlowSensitivePF", "Tracks the flow of parameters (flow sensitive)", false, false);