#include "llvm/Pass.h"
#include "llvm/IR/Instructions.h"
#include "llvm/IR/Module.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/ADT/DenseSet.h"
#include "llvm/IR/IRBuilder.h"
#include "llvm/IR/TypeBuilder.h"

using namespace llvm;

namespace {
	struct PointerObservation : public FunctionPass {
		static char ID;
		PointerObservation() : FunctionPass(ID) {}

		bool runOnFunction(Function& f) override {

      // Identify injection points
      DenseSet<Instruction*> pointerInstructions;
      for(BasicBlock &bb : f) {
        	for(Instruction &i : bb) {
        		if (GetElementPtrInst* gep = dyn_cast<GetElementPtrInst>(&i)) {
              pointerInstructions.insert(gep);
        		}
        	}
      }

      // Inject instructions
      for (Instruction *i : pointerInstructions) {
        errs() << "Injecting print operation\n";
        IRBuilder<> builder(i);
        Function *printfFunction = getPrintFPrototype(i->getContext(), i->getModule());

        builder.CreateCall(printfFunction, geti8StrVal(*i->getModule(), "Pointer instruction\n", "name"));
      }

			return true;
		}

    Function *getPrintFPrototype(LLVMContext &ctx, Module *mod) {
      FunctionType *printf_type = TypeBuilder<int(char*, ...), false>::get(getGlobalContext());
      Function *func = cast<Function>(mod->getOrInsertFunction("printf", printf_type, AttributeSet().addAttribute(mod->getContext(), 1U, Attribute::NoAlias)));
      return func;
    }

    Constant* geti8StrVal(Module& M, char const* str, Twine const& name) {
      LLVMContext& ctx = getGlobalContext();
      Constant* strConstant = ConstantDataArray::getString(ctx, str);
      GlobalVariable* GVStr =
          new GlobalVariable(M, strConstant->getType(), true,
                             GlobalValue::InternalLinkage, strConstant, name);
      Constant* zero = Constant::getNullValue(IntegerType::getInt32Ty(ctx));
      Constant* indices[] = {zero, zero};
      Constant* strVal = ConstantExpr::getGetElementPtr(0, GVStr, indices, true);
      return strVal;
    }

	};

}

char PointerObservation::ID = 0;
static RegisterPass<PointerObservation> X("PointerObservation", "Injects print calls before pointer operations", false, false);