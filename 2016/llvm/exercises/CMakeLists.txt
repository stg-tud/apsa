cmake_minimum_required(VERSION 2.8)
find_package(LLVM REQUIRED CONFIG)

message(STATUS "Found LLVM ${LLVM_PACKAGE_VERSION}")
message(STATUS "Using LLVMConfig.cmake in: ${LLVM_DIR}")

include_directories(${LLVM_INCLUDE_DIRS})
message(STATUS "Using includes in: ${LLVM_INCLUDE_DIRS}")
add_definitions(${LLVM_DEFINITIONS})
message(STATUS "Using definitions in in: ${LLVM_DEFINITIONS}")
add_definitions("-std=c++11")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fno-rtti -std=c++11")

add_subdirectory(FirstPass)
add_subdirectory(InstructionCount)
