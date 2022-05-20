#!/bin/bash

set -exo pipefail

# Vector size 4

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v4_bin.t2 --Lattigo --w 4 \
--config src/test/resources/manhattan_dist/configs/lattigo-bfv-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v4_bin.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v4_bin_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v4_bin.t2 --PALISADE --w 4 \
--config src/test/resources/manhattan_dist/configs/palisade-bfv-bin-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v4_bin.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v4_bin_PALISADE.log
cd ../..

# Vector size 8

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v8_bin.t2 --Lattigo --w 4 \
--config src/test/resources/manhattan_dist/configs/lattigo-bfv-128_v8.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v8_bin.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v8_bin_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/manhattan_dist/manhattan_dist_v8_bin.t2 --PALISADE --w 4 \
--config src/test/resources/manhattan_dist/configs/palisade-bfv-bin-128.config
cp ./src/test/resources/manhattan_dist/manhattan_dist_v8_bin.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/manhattan_dist/manhattan_dist_v8_bin_PALISADE.log
cd ../..