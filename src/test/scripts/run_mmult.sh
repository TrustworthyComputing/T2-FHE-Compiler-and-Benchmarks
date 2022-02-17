#!/bin/bash

set -exo pipefail

# 4x4

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_4x4.t2 --HELIB \
  --config src/test/resources/mmult/configs/helib-bfv-int-128-4x4.config
cp ./src/test/resources/mmult/mmult_4x4.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/mmult/mmult_4x4_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_4x4_ckks.t2 --HELIB \
--config src/test/resources/mmult/configs/helib-ckks-128.config
cp ./src/test/resources/mmult/mmult_4x4_ckks.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/mmult/mmult_4x4_ckks_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_4x4.t2 --LATTIGO \
  --config src/test/resources/mmult/configs/lattigo-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_4x4.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/mmult/mmult_4x4_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_4x4_ckks.t2 --LATTIGO \
--config src/test/resources/mmult/configs/lattigo-ckks-128.config
cp ./src/test/resources/mmult/mmult_4x4_ckks.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/mmult/mmult_4x4_ckks_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_4x4.t2 --PALISADE \
--config src/test/resources/mmult/configs/palisade-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_4x4.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/mmult/mmult_4x4_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_4x4_ckks.t2 --PALISADE \
--config src/test/resources/mmult/configs/palisade-ckks-128.config
cp ./src/test/resources/mmult/mmult_4x4_ckks.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/mmult/mmult_4x4_ckks_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_4x4.t2 --SEAL \
  --config src/test/resources/mmult/configs/seal-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_4x4.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/mmult/mmult_4x4_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_4x4_ckks.t2 --SEAL \
--config src/test/resources/mmult/configs/seal-ckks-128.config
cp ./src/test/resources/mmult/mmult_4x4_ckks.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/mmult/mmult_4x4_ckks_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_4x4.t2 --TFHE
cp ./src/test/resources/mmult/mmult_4x4.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/mmult/mmult_4x4_TFHE.log
cd ../..


# 8x8

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_8x8.t2 --HELIB \
  --config src/test/resources/mmult/configs/helib-bfv-int-128-8x8.config
cp ./src/test/resources/mmult/mmult_8x8.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/mmult/mmult_8x8_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_8x8_ckks.t2 --HELIB \
--config src/test/resources/mmult/configs/helib-ckks-128.config
cp ./src/test/resources/mmult/mmult_8x8_ckks.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/mmult/mmult_8x8_ckks_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_8x8.t2 --LATTIGO \
  --config src/test/resources/mmult/configs/lattigo-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_8x8.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/mmult/mmult_8x8_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_8x8_ckks.t2 --LATTIGO \
--config src/test/resources/mmult/configs/lattigo-ckks-128.config
cp ./src/test/resources/mmult/mmult_8x8_ckks.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/mmult/mmult_8x8_ckks_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_8x8.t2 --PALISADE \
  --config src/test/resources/mmult/configs/palisade-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_8x8.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/mmult/mmult_8x8_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_8x8_ckks.t2 --PALISADE \
--config src/test/resources/mmult/configs/palisade-ckks-128.config
cp ./src/test/resources/mmult/mmult_8x8_ckks.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/mmult/mmult_8x8_ckks_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_8x8.t2 --SEAL \
  --config src/test/resources/mmult/configs/seal-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_8x8.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/mmult/mmult_8x8_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_8x8_ckks.t2 --SEAL \
--config src/test/resources/mmult/configs/seal-ckks-128.config
cp ./src/test/resources/mmult/mmult_8x8_ckks.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/mmult/mmult_8x8_ckks_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_8x8.t2 --TFHE
cp ./src/test/resources/mmult/mmult_8x8.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/mmult/mmult_8x8_TFHE.log
cd ../..


# 16x16

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_16x16.t2 --HELIB \
  --config src/test/resources/mmult/configs/helib-bfv-int-128-16x16.config
cp ./src/test/resources/mmult/mmult_16x16.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/mmult/mmult_16x16_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_16x16_ckks.t2 --HELIB \
--config src/test/resources/mmult/configs/helib-ckks-128.config
cp ./src/test/resources/mmult/mmult_16x16_ckks.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/mmult/mmult_16x16_ckks_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_16x16.t2 --LATTIGO \
  --config src/test/resources/mmult/configs/lattigo-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_16x16.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/mmult/mmult_16x16_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_16x16_ckks.t2 --LATTIGO \
--config src/test/resources/mmult/configs/lattigo-ckks-128.config
cp ./src/test/resources/mmult/mmult_16x16_ckks.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/mmult/mmult_16x16_ckks_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_16x16.t2 --PALISADE \
  --config src/test/resources/mmult/configs/palisade-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_16x16.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/mmult/mmult_16x16_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_16x16_ckks.t2 --PALISADE \
--config src/test/resources/mmult/configs/palisade-ckks-128.config
cp ./src/test/resources/mmult/mmult_16x16_ckks.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/mmult/mmult_16x16_ckks_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_16x16.t2 --SEAL \
  --config src/test/resources/mmult/configs/seal-bfv-int-128.config
cp ./src/test/resources/mmult/mmult_16x16.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/mmult/mmult_16x16_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/mmult/mmult_16x16_ckks.t2 --SEAL \
--config src/test/resources/mmult/configs/seal-ckks-128.config
cp ./src/test/resources/mmult/mmult_16x16_ckks.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/mmult/mmult_16x16_ckks_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_16x16.t2 --TFHE
cp ./src/test/resources/mmult/mmult_16x16.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/mmult/mmult_16x16_TFHE.log
cd ../..