#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/IR/Instructions.h"
#include "llvm/IR/IRBuilder.h"
#include "llvm/Support/raw_ostream.h"

using namespace llvm;

namespace {
	struct FirstTransform : public FunctionPass {
    	static char ID;
    	FirstTransform() : FunctionPass(ID) {}

    	bool runOnFunction(Function &F) override {
      		errs() << "Function recognized: ";
      		errs().write_escaped(F.getName());
      		errs() << '\n';



          for(BasicBlock &bb : F) {


            Instruction *firstInst = bb.getFirstNonPHI();
            IRBuilder<> builder(firstInst);
            Instruction *newInst = builder.CreateAlloca(Type::getInt32Ty(bb.getContext()));

            //bb.getInstList().insert(firstInst, newInst);

          }


      		return true;
    	}	
  	};
}

char FirstTransform::ID = 0;
static RegisterPass<FirstTransform> X("FirstTransform", "First transform", false, false);
