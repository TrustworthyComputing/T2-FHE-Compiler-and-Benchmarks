#!/bin/bash

set -exo pipefail

# Integer Domain

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --HELIB \
  --config src/test/resources/tests/core_operations/configs/helib-16k-128-int.config
cp ./src/test/resources/tests/core_operations/add_mult.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_int_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --SEAL \
  --config src/test/resources/tests/core_operations/configs/seal-16k-128-int.config
cp ./src/test/resources/tests/core_operations/add_mult.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_int_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --PALISADE \
  --config src/test/resources/tests/core_operations/configs/palisade-16k-128-int.config
cp ./src/test/resources/tests/core_operations/add_mult.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_int_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --LATTIGO \
  --config src/test/resources/tests/core_operations/configs/lattigo-16k-128-int.config
cp ./src/test/resources/tests/core_operations/add_mult.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_int_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/comp.t2 --HELIB \
  --config src/test/resources/tests/core_operations/configs/helib-16k-128-int.config
cp ./src/test/resources/tests/core_operations/comp.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/core_operations/logs/comp_int_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/comp.t2 --SEAL \
  --config src/test/resources/tests/core_operations/configs/seal-16k-128-int.config
cp ./src/test/resources/tests/core_operations/comp.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/core_operations/logs/comp_int_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/comp.t2 --PALISADE \
  --config src/test/resources/tests/core_operations/configs/palisade-16k-128-int.config
cp ./src/test/resources/tests/core_operations/comp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/core_operations/logs/comp_int_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/comp.t2 --LATTIGO \
  --config src/test/resources/tests/core_operations/configs/lattigo-16k-128-int.config
cp ./src/test/resources/tests/core_operations/comp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/core_operations/logs/comp_int_Lattigo.log
cd ../..

# Binary Domain

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --HELIB --w 8 \
  --config src/test/resources/tests/core_operations/configs/helib-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/add_mult.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_bin_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --SEAL --w 8 \
  --config src/test/resources/tests/core_operations/configs/seal-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/add_mult.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_bin_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --PALISADE --w 8 \
  --config src/test/resources/tests/core_operations/configs/palisade-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/add_mult.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_bin_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --LATTIGO --w 8 \
  --config src/test/resources/tests/core_operations/configs/lattigo-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/add_mult.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_bin_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult.t2 --TFHE --w 8
cp ./src/test/resources/tests/core_operations/add_mult.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_bin_TFHE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/comp.t2 --HELIB --w 8 \
  --config src/test/resources/tests/core_operations/configs/helib-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/comp.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/core_operations/logs/comp_bin_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/comp.t2 --SEAL --w 8 \
  --config src/test/resources/tests/core_operations/configs/seal-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/comp.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/core_operations/logs/comp_bin_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/comp.t2 --PALISADE --w 8 \
  --config src/test/resources/tests/core_operations/configs/palisade-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/comp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/core_operations/logs/comp_bin_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/comp.t2 --LATTIGO --w 8 \
  --config src/test/resources/tests/core_operations/configs/lattigo-32k-128-bin.config
cp ./src/test/resources/tests/core_operations/comp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/core_operations/logs/comp_bin_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/comp.t2 --TFHE --w 8
cp ./src/test/resources/tests/core_operations/comp.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/tests/core_operations/logs/comp_bin_TFHE.log
cd ../..

# Floating Point Domain

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult_fp.t2 --HELIB \
  --config src/test/resources/tests/core_operations/configs/helib-16k-128-fp.config
cp ./src/test/resources/tests/core_operations/add_mult_fp.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_fp_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult_fp.t2 --SEAL \
  --config src/test/resources/tests/core_operations/configs/seal-16k-128-fp.config
cp ./src/test/resources/tests/core_operations/add_mult_fp.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_fp_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult_fp.t2 --PALISADE \
  --config src/test/resources/tests/core_operations/configs/palisade-16k-128-fp.config
cp ./src/test/resources/tests/core_operations/add_mult_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_fp_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/tests/core_operations/add_mult_fp.t2 --LATTIGO \
  --config src/test/resources/tests/core_operations/configs/lattigo-16k-128-fp.config
cp ./src/test/resources/tests/core_operations/add_mult_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/core_operations/logs/add_mult_fp_Lattigo.log
cd ../..
