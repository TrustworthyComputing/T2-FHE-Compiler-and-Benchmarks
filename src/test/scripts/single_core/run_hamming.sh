#!/bin/bash

set -exo pipefail

# Vector size 4

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v4.t2 --Lattigo --w 4 \
--config src/test/resources/ham_dist/configs/lattigo-bfv-128-v4.config
cp ./src/test/resources/ham_dist/hamming_distance_v4.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/ham_dist/hamming_distance_v4_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v4.t2 --PALISADE --w 4 \
--config src/test/resources/ham_dist/configs/palisade-bfv-128-v4.config
cp ./src/test/resources/ham_dist/hamming_distance_v4.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/ham_dist/hamming_distance_v4_PALISADE.log
cd ../..

# Vector size 8

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v8.t2 --Lattigo --w 4 \
--config src/test/resources/ham_dist/configs/lattigo-bfv-128-v8.config
cp ./src/test/resources/ham_dist/hamming_distance_v8.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/ham_dist/hamming_distance_v8_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v8.t2 --PALISADE --w 4 \
--config src/test/resources/ham_dist/configs/palisade-bfv-128-v8.config
cp ./src/test/resources/ham_dist/hamming_distance_v8.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/ham_dist/hamming_distance_v8_PALISADE.log
cd ../..