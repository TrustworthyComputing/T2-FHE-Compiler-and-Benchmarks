#!/bin/bash

set -exo pipefail

# Vector size 4

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v4.t2 --PALISADE \
--config src/test/resources/ham_dist/configs/palisade-bgv-128-v4-int.config
cp ./src/test/resources/ham_dist/hamming_distance_v4.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/ham_dist/hamming_distance_v4_int_bgv_PALISADE.log
taskset 1 ./bin/test.out > ../test/resources/ham_dist/hamming_distance_v4_int_bgv_single_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v4.t2 --SEAL \
--config src/test/resources/ham_dist/configs/seal-bgv-128-v4.config
cp ./src/test/resources/ham_dist/hamming_distance_v4.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/ham_dist/hamming_distance_v4_int_bgv_SEAL.log
cd ../..

# Vector size 8

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v8.t2 --PALISADE \
--config src/test/resources/ham_dist/configs/palisade-bgv-128-v4-int.config
cp ./src/test/resources/ham_dist/hamming_distance_v8.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/ham_dist/hamming_distance_v8_int_bgv_PALISADE.log
taskset 1 ./bin/test.out > ../test/resources/ham_dist/hamming_distance_v8_int_bgv_single_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/ham_dist/hamming_distance_v8.t2 --SEAL \
--config src/test/resources/ham_dist/configs/seal-bgv-128-v4.config
cp ./src/test/resources/ham_dist/hamming_distance_v8.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/ham_dist/hamming_distance_v8_int_bgv_SEAL.log
cd ../..
