#!/bin/bash

set -exo pipefail

# Binary 4

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_4_bin.t2 --HELIB --w 5 \
--config src/test/resources/pir/configs/helib-bgv-128-bin.config
cp ./src/test/resources/pir/pir_4_bin.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/pir/pir_4_bin_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_4_bin.t2 --Lattigo --w 5 \
--config src/test/resources/pir/configs/lattigo-bfv-128-bin.config
cp ./src/test/resources/pir/pir_4_bin.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/pir/pir_4_bin_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_4_bin.t2 --PALISADE --w 5 \
--config src/test/resources/pir/configs/palisade-bfv-128-bin.config
cp ./src/test/resources/pir/pir_4_bin.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/pir/pir_4_bin_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_4_bin.t2 --SEAL --w 5 \
--config src/test/resources/pir/configs/seal-bfv-128-bin.config
cp ./src/test/resources/pir/pir_4_bin.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/pir/pir_4_bin_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_4_bin.t2 --TFHE --w 5 
cp ./src/test/resources/pir/pir_4_bin.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/pir/pir_4_bin_TFHE.log
cd ../..


# Binary 8

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_8_bin.t2 --HELIB --w 5 \
--config src/test/resources/pir/configs/helib-bgv-128-bin.config
cp ./src/test/resources/pir/pir_8_bin.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/pir/pir_8_bin_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_8_bin.t2 --Lattigo --w 5 \
--config src/test/resources/pir/configs/lattigo-bfv-128-bin-big.config
cp ./src/test/resources/pir/pir_8_bin.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/pir/pir_8_bin_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_8_bin.t2 --PALISADE --w 5 \
--config src/test/resources/pir/configs/palisade-bfv-128-bin-8.config
cp ./src/test/resources/pir/pir_8_bin.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/pir/pir_8_bin_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_8_bin.t2 --SEAL --w 5 \
--config src/test/resources/pir/configs/seal-bfv-128-bin.config
cp ./src/test/resources/pir/pir_8_bin.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/pir/pir_8_bin_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_8_bin.t2 --TFHE --w 5 
cp ./src/test/resources/pir/pir_8_bin.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/pir/pir_8_bin_TFHE.log
cd ../..


# Binary 16

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_16_bin.t2 --HELIB --w 5 \
--config src/test/resources/pir/configs/helib-bgv-128-bin.config
cp ./src/test/resources/pir/pir_16_bin.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/pir/pir_16_bin_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_16_bin.t2 --Lattigo --w 5 \
--config src/test/resources/pir/configs/lattigo-bfv-128-bin-big.config
cp ./src/test/resources/pir/pir_16_bin.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/pir/pir_16_bin_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_16_bin.t2 --PALISADE --w 5 \
--config src/test/resources/pir/configs/palisade-bfv-128-bin-16.config
cp ./src/test/resources/pir/pir_16_bin.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/pir/pir_16_bin_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_16_bin.t2 --TFHE --w 5 
cp ./src/test/resources/pir/pir_16_bin.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/pir/pir_16_bin_TFHE.log
cd ../..
