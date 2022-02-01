cmake_minimum_required(VERSION 3.13)

project(SEALExamples VERSION 3.7.1 LANGUAGES CXX)

# Import Microsoft SEAL
find_package(SEAL 3.7.1 EXACT REQUIRED)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/bin)

if(TARGET SEAL::seal)
  link_libraries(SEAL::seal)
elseif(TARGET SEAL::seal_shared)
  link_libraries(SEAL::seal_shared)
else()
  message(FATAL_ERROR "Cannot find target SEAL::seal or SEAL::seal_shared")
endif()

add_executable(factorial-bfv.out)
target_sources(factorial-bfv.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/Microbenchmarks/factorial-bfv/factorial-bfv.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.hpp
)

add_executable(fibonacci-bfv.out)
target_sources(fibonacci-bfv.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/Microbenchmarks/fibonacci-bfv/fibonacci-bfv.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.hpp
)

add_executable(simon-bfv.out)
target_sources(simon-bfv.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/Crypto/simon-bfv/simon-bfv.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.hpp
)

add_executable(simon-bfv-batched.out)
target_sources(simon-bfv-batched.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/Crypto/simon-bfv/simon-bfv-batched.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.hpp
)

add_executable(lr-bfv-batched.out)
target_sources(lr-bfv-batched.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/ML-Inference/lr/lr-bfv-batched.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.hpp
)

add_executable(test.out)
target_sources(test.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/compiled/test.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.hpp
)
