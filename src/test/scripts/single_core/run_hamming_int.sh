#!/bin/bash

set -exo pipefail

# Vector size 4

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v4.t2 --Lattigo \
--config src/test/resources/ham_dist/configs/lattigo-bfv-128-int.config
cp ./src/test/resources/ham_dist/hamming_distance_v4.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/ham_dist/hamming_distance_v4_int_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v4.t2 --PALISADE \
--config src/test/resources/ham_dist/configs/palisade-bfv-128-v4-int.config
cp ./src/test/resources/ham_dist/hamming_distance_v4.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/ham_dist/hamming_distance_v4_int_PALISADE.log
cd ../..

# Vector size 8

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v8.t2 --Lattigo \
--config src/test/resources/ham_dist/configs/lattigo-bfv-128-int.config
cp ./src/test/resources/ham_dist/hamming_distance_v8.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/ham_dist/hamming_distance_v8_int_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v8.t2 --PALISADE \
--config src/test/resources/ham_dist/configs/palisade-bfv-128-v4-int.config
cp ./src/test/resources/ham_dist/hamming_distance_v8.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/ham_dist/hamming_distance_v8_int_PALISADE.log
cd ../..