#!/bin/bash

set -exo pipefail

# HElib

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate.t2 --HELIB \
  --config src/test/resources/tests/core_operations/configs/helib-16k-128-int.config
cp ./src/test/resources/tests/core_operations/rotate.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_int_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate.t2 --w 8 --HELIB \
  --config src/test/resources/tests/core_operations/configs/helib-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/rotate.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_bin_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate_ckks.t2 --HELIB \
  --config src/test/resources/tests/core_operations/configs/helib-16k-128-fp.config
cp ./src/test/resources/tests/core_operations/rotate_ckks.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_fp_HElib.log
cd ../..

# SEAL

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate.t2 --SEAL \
  --config src/test/resources/tests/core_operations/configs/seal-16k-128-int.config
cp ./src/test/resources/tests/core_operations/rotate.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_int_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate.t2 --w 8 --SEAL \
  --config src/test/resources/tests/core_operations/configs/seal-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/rotate.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_bin_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate_ckks.t2 --SEAL \
  --config src/test/resources/tests/core_operations/configs/seal-16k-128-fp.config
cp ./src/test/resources/tests/core_operations/rotate_ckks.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_fp_SEAL.log
cd ../..

# PALISADE

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate.t2 --PALISADE \
  --config src/test/resources/tests/core_operations/configs/palisade-16k-128-int.config
cp ./src/test/resources/tests/core_operations/rotate.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_int_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate.t2 --w 8 --PALISADE \
  --config src/test/resources/tests/core_operations/configs/palisade-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/rotate.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_bin_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate_ckks.t2 --PALISADE \
  --config src/test/resources/tests/core_operations/configs/palisade-16k-128-fp.config
cp ./src/test/resources/tests/core_operations/rotate_ckks.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_fp_PALISADE.log
cd ../..

# Lattigo

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate.t2 --Lattigo \
  --config src/test/resources/tests/core_operations/configs/lattigo-16k-128-int.config
cp ./src/test/resources/tests/core_operations/rotate.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_int_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate.t2 --w 8 --Lattigo \
  --config src/test/resources/tests/core_operations/configs/lattigo-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/rotate.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_bin_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate_ckks.t2 --Lattigo \
  --config src/test/resources/tests/core_operations/configs/lattigo-16k-128-fp.config
cp ./src/test/resources/tests/core_operations/rotate_ckks.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_fp_Lattigo.log
cd ../..

# TFHE

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/rotate.t2 --w 8 --TFHE \
cp ./src/test/resources/tests/core_operations/rotate.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/tests/core_operations/rotate_bin_TFHE.log
cd ../..
