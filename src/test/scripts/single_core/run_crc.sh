#!/bin/bash

set -exo pipefail

# Binary Domain

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --LATTIGO --w 8 \
  --config src/test/resources/crc/configs/lattigo-bfv-crc8-128.config
cp ./src/test/resources/crc/crc8.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/crc/crc_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --PALISADE --w 8 \
  --config src/test/resources/crc/configs/palisade-bfv-crc8-128.config
cp ./src/test/resources/crc/crc8.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/crc/crc_PALISADE.log
cd ../..