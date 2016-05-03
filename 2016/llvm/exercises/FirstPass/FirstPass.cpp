#include "llvm/Pass.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"

using namespace llvm;

namespace {
	struct FirstPass : public FunctionPass {
    	static char ID;
    	FirstPass() : FunctionPass(ID) {}

    	bool runOnFunction(Function &F) override {
      		errs() << "Function recognized: ";
      		errs().write_escaped(F.getName());
      		errs() << '\n';
      		return false;
    	}	
  	};
}

char FirstPass::ID = 0;
static RegisterPass<FirstPass> X("FirstPass", "Function printer pass", false, false);
