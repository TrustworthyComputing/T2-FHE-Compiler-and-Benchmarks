#!/bin/bash

set -exo pipefail

# Integer 64

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_64_int.t2 --Lattigo \
--config src/test/resources/pir/configs/lattigo-bfv-128-int.config
cp ./src/test/resources/pir/pir_64_int.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_64_int_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_64_int.t2 --PALISADE \
--config src/test/resources/pir/configs/palisade-bfv-128-int.config
cp ./src/test/resources/pir/pir_64_int.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_64_int_PALISADE.log
cd ../..

# Integer 128

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_128_int.t2 --Lattigo \
--config src/test/resources/pir/configs/lattigo-bfv-128-int.config
cp ./src/test/resources/pir/pir_128_int.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_128_int_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_128_int.t2 --PALISADE \
--config src/test/resources/pir/configs/palisade-bfv-128-int.config
cp ./src/test/resources/pir/pir_128_int.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_128_int_PALISADE.log
cd ../..

# Integer 256

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_256_int.t2 --Lattigo \
--config src/test/resources/pir/configs/lattigo-bfv-128-int.config
cp ./src/test/resources/pir/pir_256_int.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_256_int_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
src/test/resources/pir/pir_256_int.t2 --PALISADE \
--config src/test/resources/pir/configs/palisade-bfv-128-int.config
cp ./src/test/resources/pir/pir_256_int.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
taskset 1 ./bin/test.out > ../test/resources/pir/pir_256_int_PALISADE.log
cd ../..