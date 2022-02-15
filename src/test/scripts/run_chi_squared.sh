#!/bin/bash

set -exo pipefail

# Integer

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --HELIB \
  --config src/test/resources/chi_squared/configs/helib-bfv-128.config
cp ./src/test/resources/chi_squared/chi_squared.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/chi_squared/chi_squared_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --LATTIGO
cp ./src/test/resources/chi_squared/chi_squared.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/chi_squared/chi_squared_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --PALISADE \
  --config src/test/resources/chi_squared/configs/palisade-bfv-128.config
cp ./src/test/resources/chi_squared/chi_squared.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/chi_squared/chi_squared_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --SEAL \
  --config src/test/resources/chi_squared/configs/seal-bfv-128.config
cp ./src/test/resources/chi_squared/chi_squared.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/chi_squared/chi_squared_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --TFHE
cp ./src/test/resources/chi_squared/chi_squared.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/chi_squared/chi_squared_TFHE.log
cd ../..


# Floating-Point

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared_ckks.t2 --HELIB \
  --config src/test/resources/chi_squared/configs/helib-ckks-128.config
cp ./src/test/resources/chi_squared/chi_squared_ckks.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/chi_squared/chi_squared_HElib_ckks.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared_ckks.t2 --LATTIGO \
  --config src/test/resources/chi_squared/configs/lattigo-ckks-128.config
cp ./src/test/resources/chi_squared/chi_squared_ckks.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/chi_squared/chi_squared_Lattigo_ckks.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared_ckks.t2 --PALISADE \
  --config src/test/resources/chi_squared/configs/palisade-ckks-128.config
cp ./src/test/resources/chi_squared/chi_squared_ckks.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/chi_squared/chi_squared_PALISADE_ckks.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared_ckks.t2 --SEAL \
  --config src/test/resources/chi_squared/configs/seal-ckks-256.config
cp ./src/test/resources/chi_squared/chi_squared_ckks.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/chi_squared/chi_squared_SEAL_ckks.log
cd ../..
