#!/bin/bash

set -exo pipefail

# Binary Domain

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/batcher_sort/batcher_sort_4.t2 --HELIB --w 4 \
--config src/test/resources/batcher_sort/configs/helib-bgv-boot.config
cp ./src/test/resources/batcher_sort/batcher_sort_4.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/batcher_sort/batcher_sort_4_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/batcher_sort/batcher_sort_8.t2 --HELIB --w 4 \
--config src/test/resources/batcher_sort/configs/helib-bgv-boot.config
cp ./src/test/resources/batcher_sort/batcher_sort_8.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/batcher_sort/batcher_sort_8_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/batcher_sort/batcher_sort_16.t2 --HELIB --w 4 \
--config src/test/resources/batcher_sort/configs/helib-bgv-boot.config
cp ./src/test/resources/batcher_sort/batcher_sort_16.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/batcher_sort/batcher_sort_16_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/batcher_sort/batcher_sort_4.t2 --TFHE --w 4
cp ./src/test/resources/batcher_sort/batcher_sort_4.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/batcher_sort/batcher_sort_4_TFHE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/batcher_sort/batcher_sort_8.t2 --TFHE --w 4
cp ./src/test/resources/batcher_sort/batcher_sort_8.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/batcher_sort/batcher_sort_8_TFHE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/batcher_sort/batcher_sort_16.t2 --TFHE --w 4
cp ./src/test/resources/batcher_sort/batcher_sort_16.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/batcher_sort/batcher_sort_16_TFHE.log
cd ../..