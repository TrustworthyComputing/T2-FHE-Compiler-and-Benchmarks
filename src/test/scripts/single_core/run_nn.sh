#!/bin/bash

set -exo pipefail

# 50 Neurons

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_50.t2 --LATTIGO \
--config src/test/resources/nn_inference/configs/lattigo-bfv-128.config
cp ./src/test/resources/nn_inference/nn_50.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_50_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_50_fp.t2 --LATTIGO \
--config src/test/resources/nn_inference/configs/lattigo-ckks-128.config
cp ./src/test/resources/nn_inference/nn_50_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_50_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_50.t2 --PALISADE \
--config src/test/resources/nn_inference/configs/palisade-bfv-128.config
cp ./src/test/resources/nn_inference/nn_50.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_50_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_50_fp.t2 --PALISADE \
--config src/test/resources/nn_inference/configs/palisade-ckks-128.config
cp ./src/test/resources/nn_inference/nn_50_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_50_fp_PALISADE.log
cd ../..

# 100 Neurons

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_100.t2 --LATTIGO \
--config src/test/resources/nn_inference/configs/lattigo-bfv-128.config
cp ./src/test/resources/nn_inference/nn_100.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_100_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_100_fp.t2 --LATTIGO \
--config src/test/resources/nn_inference/configs/lattigo-ckks-128.config
cp ./src/test/resources/nn_inference/nn_100_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_100_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_100.t2 --PALISADE \
--config src/test/resources/nn_inference/configs/palisade-bfv-128.config
cp ./src/test/resources/nn_inference/nn_100.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_100_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_100_fp.t2 --PALISADE \
--config src/test/resources/nn_inference/configs/palisade-ckks-128.config
cp ./src/test/resources/nn_inference/nn_100_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_100_fp_PALISADE.log
cd ../..

# 16x16

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_150.t2 --LATTIGO \
--config src/test/resources/nn_inference/configs/lattigo-bfv-128.config
cp ./src/test/resources/nn_inference/nn_150.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_150_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_150_fp.t2 --LATTIGO \
--config src/test/resources/nn_inference/configs/lattigo-ckks-128.config
cp ./src/test/resources/nn_inference/nn_150_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_150_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_150.t2 --PALISADE \
--config src/test/resources/nn_inference/configs/palisade-bfv-128.config
cp ./src/test/resources/nn_inference/nn_150.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_150_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/nn_inference/nn_150_fp.t2 --PALISADE \
--config src/test/resources/nn_inference/configs/palisade-ckks-128.config
cp ./src/test/resources/nn_inference/nn_150_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/nn_inference/nn_150_fp_PALISADE.log
cd ../..