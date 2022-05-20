#!/bin/bash

set -exo pipefail

# Floating Point 64

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_64_fp.t2 --Lattigo \
--config src/test/resources/pir/configs/lattigo-ckks-128-fp.config
cp ./src/test/resources/pir/pir_64_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_64_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_64_fp.t2 --PALISADE \
--config src/test/resources/pir/configs/palisade-ckks-128-fp.config
cp ./src/test/resources/pir/pir_64_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_64_fp_PALISADE.log
cd ../..

# Floating Point 128

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_128_fp.t2 --Lattigo \
--config src/test/resources/pir/configs/lattigo-ckks-128-fp.config
cp ./src/test/resources/pir/pir_128_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_128_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_128_fp.t2 --PALISADE \
--config src/test/resources/pir/configs/palisade-ckks-128-fp.config
cp ./src/test/resources/pir/pir_128_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_128_fp_PALISADE.log
cd ../..

# Floating Point 256

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_256_fp.t2 --Lattigo \
--config src/test/resources/pir/configs/lattigo-ckks-128-fp.config
cp ./src/test/resources/pir/pir_256_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_256_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_256_fp.t2 --PALISADE \
--config src/test/resources/pir/configs/palisade-ckks-128-fp.config
cp ./src/test/resources/pir/pir_256_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_256_fp_PALISADE.log
cd ../..