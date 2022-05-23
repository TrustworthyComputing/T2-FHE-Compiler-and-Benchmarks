#!/bin/bash

set -exo pipefail

# Integer

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --PALISADE \
  --config src/test/resources/chi_squared/configs/palisade-bgv-128.config
cp ./src/test/resources/chi_squared/chi_squared.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/chi_squared/chi_squared_bgv_PALISADE.log
taskset 1 ./bin/test.out > ../test/resources/chi_squared/chi_squared_bgv_single_PALISADE.log

cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --SEAL \
  --config src/test/resources/chi_squared/configs/seal-bgv-128.config
cp ./src/test/resources/chi_squared/chi_squared.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/chi_squared/chi_squared_bgv_SEAL.log
cd ../..