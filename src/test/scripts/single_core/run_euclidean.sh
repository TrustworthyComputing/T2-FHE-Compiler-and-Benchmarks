#!/bin/bash

set -exo pipefail

# Vector size 64

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v64.t2 --Lattigo \
--config src/test/resources/euclidean_distance/configs/lattigo-bfv-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v64.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v64_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v64_ckks.t2 --Lattigo \
--config src/test/resources/euclidean_distance/configs/lattigo-ckks-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v64_ckks.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v64_ckks_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v64.t2 --PALISADE  \
--config src/test/resources/euclidean_distance/configs/palisade-bfv-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v64.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v64_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v64_ckks.t2 --PALISADE  \
--config src/test/resources/euclidean_distance/configs/palisade-ckks-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v64_ckks.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v64_ckks_PALISADE.log
cd ../..

# Vector size 128

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v128.t2 --Lattigo \
--config src/test/resources/euclidean_distance/configs/lattigo-bfv-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v128.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v128_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v128_ckks.t2 --Lattigo \
--config src/test/resources/euclidean_distance/configs/lattigo-ckks-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v128_ckks.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v128_ckks_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v128.t2 --PALISADE  \
--config src/test/resources/euclidean_distance/configs/palisade-bfv-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v128.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v128_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/euclidean_distance/euclidean_distance_v128_ckks.t2 --PALISADE  \
--config src/test/resources/euclidean_distance/configs/palisade-ckks-128.config
cp ./src/test/resources/euclidean_distance/euclidean_distance_v128_ckks.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/euclidean_distance/euclidean_distance_v128_ckks_PALISADE.log
cd ../..
