#!/bin/bash

set -exo pipefail

# Vector size 64

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v64.t2 --PALISADE  \
--config src/test/resources/euclidean_distance/configs/palisade-bgv-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v64.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v64_bgv_PALISADE.log
taskset 1 ./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v64_bgv_single_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v64.t2 --SEAL  \
--config src/test/resources/euclidean_distance/configs/seal-bgv-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v64.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v64_bgv_SEAL.log
cd ../..

# Vector size 128

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v128.t2 --PALISADE  \
--config src/test/resources/euclidean_distance/configs/palisade-bgv-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v128.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v128_bgv_PALISADE.log
taskset 1 ./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v128_bgv_single_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v128.t2 --SEAL  \
--config src/test/resources/euclidean_distance/configs/seal-bgv-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v128.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v128_bgv_SEAL.log
cd ../..