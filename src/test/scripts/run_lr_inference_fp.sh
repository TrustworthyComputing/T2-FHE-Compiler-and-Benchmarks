#!/bin/bash

set -exo pipefail

# Floating-Point

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a4_fp.t2 --HELIB \
--config src/test/resources/logistic_regression/configs/helib-ckks-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a4_fp.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a4_fp_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a8_fp.t2 --HELIB \
--config src/test/resources/logistic_regression/configs/helib-ckks-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a8_fp.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a8_fp_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a16_fp.t2 --HELIB \
--config src/test/resources/logistic_regression/configs/helib-ckks-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a16_fp.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a16_fp_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a4_fp.t2 --LATTIGO \
--config src/test/resources/logistic_regression/configs/lattigo-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a4_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a4_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a8_fp.t2 --LATTIGO \
--config src/test/resources/logistic_regression/configs/lattigo-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a8_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a8_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a16_fp.t2 --LATTIGO \
--config src/test/resources/logistic_regression/configs/lattigo-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a16_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a16_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a4_fp.t2 --PALISADE \
--config src/test/resources/logistic_regression/configs/palisade-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a4_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a4_fp_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a8_fp.t2 --PALISADE \
--config src/test/resources/logistic_regression/configs/palisade-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a8_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a8_fp_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a16_fp.t2 --PALISADE \
--config src/test/resources/logistic_regression/configs/palisade-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a16_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a16_fp_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a4_fp.t2 --SEAL \
--config src/test/resources/logistic_regression/configs/seal-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a4_fp.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a4_fp_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a8_fp.t2 --SEAL \
--config src/test/resources/logistic_regression/configs/seal-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a8_fp.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a8_fp_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/logistic_regression/logistic_regression_a16_fp.t2 --SEAL \
--config src/test/resources/logistic_regression/configs/seal-ckks-16k-128-fp.config
cp ./src/test/resources/logistic_regression/logistic_regression_a16_fp.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/logistic_regression/logistic_regression_a16_fp_SEAL.log
cd ../..