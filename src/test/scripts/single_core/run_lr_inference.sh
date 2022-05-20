#!/bin/bash

set -exo pipefail

# Integer

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a4.t2 --LATTIGO \
--config src/test/resources/logistic_regression/configs/lattigo-bfv-16k-128-int.config
cp ./src/test/resources/logistic_regression/logistic_regression_a4.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a4_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a8.t2 --LATTIGO \
--config src/test/resources/logistic_regression/configs/lattigo-bfv-16k-128-int.config
cp ./src/test/resources/logistic_regression/logistic_regression_a8.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a8_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a16.t2 --LATTIGO \
--config src/test/resources/logistic_regression/configs/lattigo-bfv-16k-128-int.config
cp ./src/test/resources/logistic_regression/logistic_regression_a16.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a16_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a4.t2 --PALISADE \
--config src/test/resources/logistic_regression/configs/palisade-bfv-16k-128-int.config
cp ./src/test/resources/logistic_regression/logistic_regression_a4.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a4_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a8.t2 --PALISADE \
--config src/test/resources/logistic_regression/configs/palisade-bfv-16k-128-int.config
cp ./src/test/resources/logistic_regression/logistic_regression_a8.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a8_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a16.t2 --PALISADE \
--config src/test/resources/logistic_regression/configs/palisade-bfv-16k-128-int.config
cp ./src/test/resources/logistic_regression/logistic_regression_a16.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a16_PALISADE.log
cd ../..

# Floating-Point

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a4_fp.t2 --LATTIGO \
--config src/test/resources/logistic_regression/configs/lattigo-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a4_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a4_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a8_fp.t2 --LATTIGO \
--config src/test/resources/logistic_regression/configs/lattigo-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a8_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a8_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a16_fp.t2 --LATTIGO \
--config src/test/resources/logistic_regression/configs/lattigo-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a16_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a16_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a4_fp.t2 --PALISADE \
--config src/test/resources/logistic_regression/configs/palisade-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a4_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a4_fp_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a8_fp.t2 --PALISADE \
--config src/test/resources/logistic_regression/configs/palisade-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a8_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a8_fp_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a16_fp.t2 --PALISADE \
--config src/test/resources/logistic_regression/configs/palisade-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a16_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a16_fp_PALISADE.log
cd ../..