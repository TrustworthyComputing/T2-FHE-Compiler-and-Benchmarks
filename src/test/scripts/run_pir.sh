#!/bin/bash

set -exo pipefail

# Integer

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_int.t2 --HELIB \
--config src/test/resources/pir/configs/helib-bgv-128-int.config
cp ./src/test/resources/pir/pir_int.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/pir/pir_int_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_int.t2 --Lattigo \
--config src/test/resources/pir/configs/lattigo-bfv-128-int.config
cp ./src/test/resources/pir/pir_int.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/pir/pir_int_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_int.t2 --PALISADE \
--config src/test/resources/pir/configs/palisade-bfv-128-int.config
cp ./src/test/resources/pir/pir_int.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/pir/pir_int_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_int.t2 --SEAL \
--config src/test/resources/pir/configs/seal-bfv-128-int.config
cp ./src/test/resources/pir/pir_int.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/pir/pir_int_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_int.t2 --TFHE --w 8
cp ./src/test/resources/pir/pir_int.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/pir/pir_int_TFHE.log
cd ../..

# Floating Point

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_fp.t2 --HELIB \
--config src/test/resources/pir/configs/helib-ckks-128-fp.config
cp ./src/test/resources/pir/pir_fp.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/pir/pir_fp_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_fp.t2 --Lattigo \
--config src/test/resources/pir/configs/lattigo-ckks-128-fp.config
cp ./src/test/resources/pir/pir_fp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/pir/pir_fp_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_fp.t2 --PALISADE \
--config src/test/resources/pir/configs/palisade-ckks-128-fp.config
cp ./src/test/resources/pir/pir_fp.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/pir/pir_fp_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_fp.t2 --SEAL \
--config src/test/resources/pir/configs/seal-ckks-128-fp.config
cp ./src/test/resources/pir/pir_fp.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/pir/pir_fp_SEAL.log
cd ../..

# Binary

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_bin.t2 --HELIB --w 8 \
--config src/test/resources/pir/configs/helib-bgv-128-bin.config
cp ./src/test/resources/pir/pir_bin.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/pir/pir_bin_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_bin.t2 --Lattigo --w 8 \
--config src/test/resources/pir/configs/lattigo-bfv-128-bin.config
cp ./src/test/resources/pir/pir_bin.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/pir/pir_bin_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_bin.t2 --PALISADE --w 8 \
--config src/test/resources/pir/configs/palisade-bfv-128-bin.config
cp ./src/test/resources/pir/pir_bin.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/pir/pir_bin_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_bin.t2 --SEAL --w 8 \
--config src/test/resources/pir/configs/seal-bfv-128-bin.config
cp ./src/test/resources/pir/pir_bin.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/pir/pir_bin_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_bin.t2 --TFHE --w 8 
cp ./src/test/resources/pir/pir_bin.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/pir/pir_bin_TFHE.log
cd ../..
