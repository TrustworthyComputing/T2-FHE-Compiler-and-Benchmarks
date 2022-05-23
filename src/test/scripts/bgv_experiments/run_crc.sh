#!/bin/bash

set -exo pipefail

# Binary Domain

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --PALISADE --w 8 \
  --config src/test/resources/crc/configs/palisade-bgv-crc8-128.config
cp ./src/test/resources/crc/crc8.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/crc/crc_bgv_PALISADE.log
taskset 1 ./bin/test.out > ../test/resources/crc/crc_bgv_single_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --SEAL --w 8 \
  --config src/test/resources/crc/configs/seal-bgv-crc8-128.config
cp ./src/test/resources/crc/crc8.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/crc/crc_bgv_SEAL.log
cd ../..