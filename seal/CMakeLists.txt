# Licensed under the MIT license.

cmake_minimum_required(VERSION 3.13)

project(SEALExamples VERSION 3.7.1 LANGUAGES CXX)

# Import Microsoft SEAL
find_package(SEAL 3.7.1 EXACT REQUIRED)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_CURRENT_BINARY_DIR}/bin)

include_directories(
    ${CMAKE_CURRENT_LIST_DIR}
)

add_executable(factorial.out)

target_sources(factorial.out
    PRIVATE
        ${CMAKE_CURRENT_LIST_DIR}/Microbenchmarks/factorial/factorial.cpp
)

if(TARGET SEAL::seal)
    target_link_libraries(factorial.out PRIVATE SEAL::seal)
elseif(TARGET SEAL::seal_shared)
    target_link_libraries(factorial.out PRIVATE SEAL::seal_shared)
else()
    message(FATAL_ERROR "Cannot find target SEAL::seal or SEAL::seal_shared")
endif()


add_executable(fibonacci.out)

target_sources(fibonacci.out
    PRIVATE
        ${CMAKE_CURRENT_LIST_DIR}/Microbenchmarks/fibonacci/fibonacci.cpp
)

if(TARGET SEAL::seal)
    target_link_libraries(fibonacci.out PRIVATE SEAL::seal)
elseif(TARGET SEAL::seal_shared)
    target_link_libraries(fibonacci.out PRIVATE SEAL::seal_shared)
else()
    message(FATAL_ERROR "Cannot find target SEAL::seal or SEAL::seal_shared")
endif()
