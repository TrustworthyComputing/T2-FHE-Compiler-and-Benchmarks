cmake_minimum_required (VERSION 3.5.1)

project(palisade-examples CXX)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED True)

find_package(Palisade 1.10.6) # Palisade's ConfigVersion allows this to mean that 1.10.6 is minimum supported ver.

set( CMAKE_CXX_FLAGS ${PALISADE_CXX_FLAGS} )
set( CMAKE_EXE_LINKER_FLAGS ${PALISADE_EXE_LINKER_FLAGS} )

include_directories( ${OPENMP_INCLUDES} )
include_directories( ${PALISADE_INCLUDE} )
include_directories( ${PALISADE_INCLUDE}/third-party/include )
include_directories( ${PALISADE_INCLUDE}/core )
include_directories( ${PALISADE_INCLUDE}/pke )
include_directories( ${PALISADE_INCLUDE}/binfhe )

link_directories( ${PALISADE_LIBDIR} )
link_directories( ${OPENMP_LIBRARIES} )
link_libraries( ${PALISADE_LIBRARIES} )

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

add_executable(fibonacci-bfv-bgv.out)
target_sources(fibonacci-bfv-bgv.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/Microbenchmarks/fibonacci-bfv-bgv/fibonacci-bfv-bgv.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.hpp
)

add_executable(factorial-bfv-bgv.out)
target_sources(factorial-bfv-bgv.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/Microbenchmarks/factorial-bfv-bgv/factorial-bfv-bgv.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.hpp
)

add_executable(lr-bfv-bgv-batched.out)
target_sources(lr-bfv-bgv-batched.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/ML-Inference/lr/lr-bfv-bgv-batched.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.hpp
)

add_executable(test.out)
target_sources(test.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/compiled/test.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.hpp
)