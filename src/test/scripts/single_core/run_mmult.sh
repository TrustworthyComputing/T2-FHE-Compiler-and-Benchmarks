#!/bin/bash

set -exo pipefail

# 4x4

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_4x4.t2 --LATTIGO \
  --config src/test/resources/mmult/configs/lattigo-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_4x4.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_4x4_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_4x4_ckks.t2 --LATTIGO \
--config src/test/resources/mmult/configs/lattigo-ckks-128.config
cp ./src/test/resources/mmult/mmult_4x4_ckks.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_4x4_ckks_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_4x4.t2 --PALISADE \
--config src/test/resources/mmult/configs/palisade-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_4x4.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_4x4_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_4x4_ckks.t2 --PALISADE \
--config src/test/resources/mmult/configs/palisade-ckks-128.config
cp ./src/test/resources/mmult/mmult_4x4_ckks.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_4x4_ckks_PALISADE.log
cd ../..

# 8x8

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_8x8.t2 --LATTIGO \
  --config src/test/resources/mmult/configs/lattigo-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_8x8.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_8x8_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_8x8_ckks.t2 --LATTIGO \
--config src/test/resources/mmult/configs/lattigo-ckks-128.config
cp ./src/test/resources/mmult/mmult_8x8_ckks.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_8x8_ckks_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_8x8.t2 --PALISADE \
  --config src/test/resources/mmult/configs/palisade-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_8x8.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_8x8_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_8x8_ckks.t2 --PALISADE \
--config src/test/resources/mmult/configs/palisade-ckks-128.config
cp ./src/test/resources/mmult/mmult_8x8_ckks.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_8x8_ckks_PALISADE.log
cd ../..

# 16x16

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_16x16.t2 --LATTIGO \
  --config src/test/resources/mmult/configs/lattigo-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_16x16.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_16x16_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_16x16_ckks.t2 --LATTIGO \
--config src/test/resources/mmult/configs/lattigo-ckks-128.config
cp ./src/test/resources/mmult/mmult_16x16_ckks.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_16x16_ckks_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_16x16.t2 --PALISADE \
  --config src/test/resources/mmult/configs/palisade-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_16x16.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_16x16_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_16x16_ckks.t2 --PALISADE \
--config src/test/resources/mmult/configs/palisade-ckks-128.config
cp ./src/test/resources/mmult/mmult_16x16_ckks.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/mmult/mmult_16x16_ckks_PALISADE.log
cd ../..