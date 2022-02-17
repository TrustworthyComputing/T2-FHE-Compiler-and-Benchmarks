#!/bin/bash

set -exo pipefail

# Vector size 4

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v4_int.t2 --HELIB \
--config src/test/resources/manhattan_dist/configs/helib-bfv-int-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v4_int.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v4_int_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v4_bin.t2 --HELIB --w 4 \
--config src/test/resources/manhattan_dist/configs/helib-bfv-bin-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v4_bin.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v4_bin_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v4_bin.t2 --Lattigo --w 4 \
--config src/test/resources/manhattan_dist/configs/lattigo-bfv-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v4_bin.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v4_bin_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v4_bin.t2 --PALISADE --w 4 \
--config src/test/resources/manhattan_dist/configs/palisade-bfv-bin-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v4_bin.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v4_bin_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v4_int.t2 --SEAL \
--config src/test/resources/manhattan_dist/configs/seal-bfv-nonbatched-int-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v4_int.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v4_int_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v4_int.t2 --TFHE --w 4
cp ./src/test/resources/manhattan_dist/manhattan_dist_v4_int.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v4_int_TFHE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v4_bin.t2 --TFHE --w 4
cp ./src/test/resources/manhattan_dist/manhattan_dist_v4_bin.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v4_bin_TFHE.log
cd ../..

# Vector size 8

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v8_int.t2 --HELIB \
--config src/test/resources/manhattan_dist/configs/helib-bfv-int-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v8_int.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v8_int_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v8_bin.t2 --HELIB --w 4 \
--config src/test/resources/manhattan_dist/configs/helib-bfv-bin-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v8_bin.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v8_bin_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v8_bin.t2 --Lattigo --w 4 \
--config src/test/resources/manhattan_dist/configs/lattigo-bfv-128_v8.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v8_bin.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v8_bin_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v8_bin.t2 --PALISADE --w 4 \
--config src/test/resources/manhattan_dist/configs/palisade-bfv-bin-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v8_bin.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v8_bin_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v8_int.t2 --SEAL \
--config src/test/resources/manhattan_dist/configs/seal-bfv-nonbatched-int-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v8_int.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v8_int_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v8_int.t2 --TFHE --w 4
cp ./src/test/resources/manhattan_dist/manhattan_dist_v8_int.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v8_int_TFHE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v8_bin.t2 --TFHE --w 4
cp ./src/test/resources/manhattan_dist/manhattan_dist_v8_bin.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v8_bin_TFHE.log
cd ../..