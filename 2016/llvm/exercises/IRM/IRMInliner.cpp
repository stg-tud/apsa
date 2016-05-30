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
  struct IRMInliner : public FunctionPass {
    static char ID;
    IRMInliner() : FunctionPass(ID) {}

    bool runOnFunction(Function& f) override {

      // Identify injection points
      DenseSet<CallInst*> injectBefore;
      for(BasicBlock &bb : f) {
          for(Instruction &i : bb) {
            if (matchesEvent(&i)) {
              injectBefore.insert(cast<CallInst>(&i));
            }
          }
      }

      // Inject instructions
      for (CallInst *i : injectBefore) {
        errs() << "Injecting IRM call\n";
        IRBuilder<> builder(i);
        Function *irmCall = getIRMCallPrototype(i->getContext(), i->getModule());

        Value *irmCall_params[] = {i->getArgOperand(0), ConstantInt::get(i->getArgOperand(0)->getType(), 42)};

        builder.CreateCall(irmCall, irmCall_params);
      }

      return true;
    }

    /// \brief Determines if an instruction is a valid security event
    bool matchesEvent(Instruction* i) {
      if (i->getFunction()->getName().startswith("irm")) return false;

      CallInst* call = dyn_cast<CallInst>(i);
      if (!call) return false;
      if (call->getNumArgOperands() < 1) return false;

      Value* firstOperand = call->getArgOperand(0);

      if (!(firstOperand->getType()->isIntegerTy())) return false;

      return true;
    }

    /// \briefs Retrieves an existing reference monitor function or creates one
    Function *getIRMCallPrototype(LLVMContext &ctx, Module *mod) {
      Function *existing = mod->getFunction("irmCall");
      if(existing) return existing;

      return createReferenceMonitor(ctx, mod);
    }

    /// \brief Created a reference monitor function that compares to integers and exits if they are equal.
    Function *createReferenceMonitor(LLVMContext &ctx, Module *mod) {
      Type *i32 = IntegerType::getInt32Ty(ctx);

      // Create function prototype
      FunctionType *irmcall_type = TypeBuilder<void(int, int), false>::get(getGlobalContext());
      Function *func = cast<Function>(mod->getOrInsertFunction("irmCall", irmcall_type));

      // Create initial block loading arguments and comparing values
      IRBuilder<> *builder = new IRBuilder<>(BasicBlock::Create(ctx, "initial", func));
      // %1 = alloca i32, align 4
      Value *firstAlloc = builder->CreateAlloca(i32);
      // %2 = alloca i32, align 4
      Value *secondAlloc = builder->CreateAlloca(i32);
      // store i32 %actual, i32* %1, align 4
      Value &firstArg = func->getArgumentList().front();
      Value &secondArg = func->getArgumentList().back();
      builder->CreateStore(&firstArg, firstAlloc);
      // store i32 %expected, i32* %2, align 4#
      builder->CreateStore(&secondArg, secondAlloc);
      // %3 = load i32, i32* %1, align 4
      Value *firstLoad = builder->CreateLoad(firstAlloc);
      // %4 = load i32, i32* %2, align 4
      Value *secondLoad = builder->CreateLoad(secondAlloc);
      // %5 = icmp eq i32 %3, %4
      Value *compareResult = builder->CreateICmpEQ(firstLoad, secondLoad);
      // br i1 %5, label %6, label %9
      BasicBlock *trueCase = BasicBlock::Create(ctx, "truecase", func);
      BasicBlock *falseCase = BasicBlock::Create(ctx, "falsecase", func);
      builder->CreateCondBr(compareResult, trueCase, falseCase);

      // Create basic block for the true case (values are equal) -> function terminates execution
      IRBuilder<> *trueBuilder = new IRBuilder<>(trueCase);
      // %7 = load i32, i32* %1, align 4
      Value *thirdLoad = trueBuilder->CreateLoad(firstAlloc);
      // %8 = call i32 (i8*, ...) @printf(i8* getelementptr inbounds ([51 x i8], [51 x i8]* @.str, i32 0, i32 0), i32 %7)
      Constant *outputMessage = geti8StrVal(*mod, "Call to function with value %d detected. Exiting.\n", "outputMessage");
      Value *print_params[] = {outputMessage, thirdLoad};
      trueBuilder->CreateCall(getPrintFPrototype(ctx, mod), print_params);
      // call void @exit(i32 1) #3
      Value *exit_params[] = {ConstantInt::get(i32, 1)};
      trueBuilder->CreateCall(getExitPrototype(ctx, mod), exit_params);
      // unreachable            
      trueBuilder->CreateUnreachable();
      
      // Create basic block for the false case (values are not equal) -> function returns normally
      IRBuilder<> *falseBuilder = new IRBuilder<>(falseCase);
      // ret void
      falseBuilder->CreateRetVoid();
      return func;
    }

    /// \brief Provides a pointer to the printf() function.
    Function *getPrintFPrototype(LLVMContext &ctx, Module *mod) {
      FunctionType *printf_type = TypeBuilder<int(char*, ...), false>::get(getGlobalContext());
      Function *func = cast<Function>(mod->getOrInsertFunction("printf", printf_type, AttributeSet().addAttribute(mod->getContext(), 1U, Attribute::NoAlias)));
      return func;
    }

    /// \brief Provides a pointer to the exit() function.
    Function *getExitPrototype(LLVMContext &ctx, Module *mod) {
      FunctionType *ftype = TypeBuilder<void(int), false>::get(getGlobalContext());
      Function *func = cast<Function>(mod->getOrInsertFunction("exit", ftype));
      return func;
    }

    /// \brief Creates a constant string in the module and provides a constant pointer to it.
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

char IRMInliner::ID = 0;
static RegisterPass<IRMInliner> X("IRM", "Injects an inline reference monitor", false, false);