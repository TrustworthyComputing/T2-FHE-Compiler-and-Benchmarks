cmake_minimum_required(VERSION 3.13)

project(SEALExamples VERSION 4.1.1 LANGUAGES CXX)

# Import Microsoft SEAL
find_package(SEAL 4.1.1 EXACT REQUIRED)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/bin)

if(TARGET SEAL::seal)
  link_libraries(SEAL::seal)
elseif(TARGET SEAL::seal_shared)
  link_libraries(SEAL::seal_shared)
else()
  message(FATAL_ERROR "Cannot find target SEAL::seal or SEAL::seal_shared")
endif()

add_executable(test.out)
target_sources(test.out PRIVATE
  ${CMAKE_CURRENT_LIST_DIR}/compiled/test.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.cpp
  ${CMAKE_CURRENT_LIST_DIR}/functional_units/functional_units.hpp
)
