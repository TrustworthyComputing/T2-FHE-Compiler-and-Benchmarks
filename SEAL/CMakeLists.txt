cmake_minimum_required(VERSION 3.13)

project(SEALExamples VERSION 3.7.1 LANGUAGES CXX)

# Import Microsoft SEAL
find_package(SEAL 3.7.1 EXACT REQUIRED)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/bin)

add_executable(factorial-bfv.out)
target_sources(factorial-bfv.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/Microbenchmarks/factorial-bfv/factorial-bfv.cpp
  ${CMAKE_CURRENT_LIST_DIR}/helper.hpp
)
if(TARGET SEAL::seal)
  target_link_libraries(factorial-bfv.out PRIVATE SEAL::seal)
elseif(TARGET SEAL::seal_shared)
  target_link_libraries(factorial-bfv.out PRIVATE SEAL::seal_shared)
else()
  message(FATAL_ERROR "Cannot find target SEAL::seal or SEAL::seal_shared")
endif()

add_executable(fibonacci-bfv.out)
target_sources(fibonacci-bfv.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/Microbenchmarks/fibonacci-bfv/fibonacci-bfv.cpp
  ${CMAKE_CURRENT_LIST_DIR}/helper.hpp
)
if(TARGET SEAL::seal)
  target_link_libraries(fibonacci-bfv.out PRIVATE SEAL::seal)
elseif(TARGET SEAL::seal_shared)
  target_link_libraries(fibonacci-bfv.out PRIVATE SEAL::seal_shared)
else()
  message(FATAL_ERROR "Cannot find target SEAL::seal or SEAL::seal_shared")
endif()
