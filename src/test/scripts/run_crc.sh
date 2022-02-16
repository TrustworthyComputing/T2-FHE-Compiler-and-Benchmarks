#!/bin/bash

set -exo pipefail

# Binary Domain

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --HELIB --w 8 \
  --config src/test/resources/crc/configs/helib-bfv-crc8-128.config
cp ./src/test/resources/crc/crc8.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/crc/crc_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --LATTIGO --w 8 \
  --config src/test/resources/crc/configs/lattigo-bfv-crc8-128.config
cp ./src/test/resources/crc/crc8.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/crc/crc_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --PALISADE --w 8 \
  --config src/test/resources/crc/configs/palisade-bfv-crc8-128.config
cp ./src/test/resources/crc/crc8.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/crc/crc_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --SEAL --w 8 \
  --config src/test/resources/crc/configs/seal-bfv-crc8-128.config
cp ./src/test/resources/crc/crc8.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/crc/crc_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --TFHE --w 8 \
cp ./src/test/resources/crc/crc8.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/crc/crc_TFHE.log
cd ../..
