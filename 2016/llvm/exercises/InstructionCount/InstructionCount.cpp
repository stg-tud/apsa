#include "llvm/Pass.h"
#include "llvm/IR/Module.h"
#include "llvm/IR/Function.h"
#include "llvm/Support/raw_ostream.h"
#include "llvm/IR/InstIterator.h"

using namespace llvm;

namespace {
  struct InstructionCount : public ModulePass {
      static char ID;
      InstructionCount() : ModulePass(ID) {}

      bool runOnModule(Module &M) override {
        
        DenseMap<unsigned int, uint64_t> counts;

          for(Function &fun : M) {
            for(BasicBlock &bb : fun) {
              for(Instruction &i : bb) {
                unsigned int currentOpcode = i.getOpcode();
                
                auto instCount = counts.find(currentOpcode);
                if (counts.end() == instCount) {
                  instCount = counts.insert(std::make_pair(currentOpcode, 0)).first;
                }
                ++instCount->second;
              }
            }
          }

          for(auto &instCount : counts) {
              const char* currentOpcodeName = Instruction::getOpcodeName(instCount.first);
              errs().write_escaped(currentOpcodeName);
              errs() << " - ";
              errs() << instCount.second;
              errs() << '\n';
          }

          return false;
      } 
    };
}

char InstructionCount::ID = 0;
static RegisterPass<InstructionCount> X("InstructionCount", "Counts per module", false, false);
