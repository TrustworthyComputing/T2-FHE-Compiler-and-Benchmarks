#!/bin/bash

set -exo pipefail

# 10 Neurons

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_10.t2 --TFHE -w 16
cp ./src/test/resources/nn_inference/nn_10.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/nn_inference/nn_10_TFHE.log
cd ../..

# 20 Neurons

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_20.t2 --TFHE -w 16
cp ./src/test/resources/nn_inference/nn_20.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/nn_inference/nn_20_TFHE.log
cd ../..
