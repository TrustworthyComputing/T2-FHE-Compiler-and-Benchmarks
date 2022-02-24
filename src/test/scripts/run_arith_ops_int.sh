#!/bin/bash

set -exo pipefail

# Integer Domain

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --HELIB \
  --config src/test/resources/tests/core_operations/configs/helib-16k-128-int.config
cp ./src/test/resources/tests/core_operations/add_mult.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_int_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --SEAL \
  --config src/test/resources/tests/core_operations/configs/seal-16k-128-int.config
cp ./src/test/resources/tests/core_operations/add_mult.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_int_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --PALISADE \
  --config src/test/resources/tests/core_operations/configs/palisade-16k-128-int.config
cp ./src/test/resources/tests/core_operations/add_mult.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_int_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --LATTIGO \
  --config src/test/resources/tests/core_operations/configs/lattigo-16k-128-int.config
cp ./src/test/resources/tests/core_operations/add_mult.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_int_Lattigo.log
cd ../..