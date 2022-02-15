#!/bin/bash

set -exo pipefail

# 20 Iterations

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_20.t2 --HELIB \
  --config src/test/resources/fibonacci/configs/helib-bfv-int-128-20.config
cp ./src/test/resources/fibonacci/fibonacci_20.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_20_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_20.t2 --LATTIGO \
  --config src/test/resources/fibonacci/configs/lattigo-bfv-int-128-20.config
cp ./src/test/resources/fibonacci/fibonacci_20.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_20_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_20.t2 --PALISADE \
  --config src/test/resources/fibonacci/configs/palisade-bfv-int-128-20.config
cp ./src/test/resources/fibonacci/fibonacci_20.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_20_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_20.t2 --SEAL \
  --config src/test/resources/fibonacci/configs/seal-bfv-int-128-20.config
cp ./src/test/resources/fibonacci/fibonacci_20.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_20_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_20.t2 --TFHE
cp ./src/test/resources/fibonacci/fibonacci_20.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_20_TFHE.log
cd ../..


# 30 Iterations

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_30.t2 --HELIB \
  --config src/test/resources/fibonacci/configs/helib-bfv-int-128-30.config
cp ./src/test/resources/fibonacci/fibonacci_30.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_30_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_30.t2 --LATTIGO \
  --config src/test/resources/fibonacci/configs/lattigo-bfv-int-128-30.config
cp ./src/test/resources/fibonacci/fibonacci_30.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_30_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_30.t2 --PALISADE \
  --config src/test/resources/fibonacci/configs/palisade-bfv-int-128-30.config
cp ./src/test/resources/fibonacci/fibonacci_30.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_30_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_30.t2 --SEAL \
  --config src/test/resources/fibonacci/configs/seal-bfv-int-128-30.config
cp ./src/test/resources/fibonacci/fibonacci_30.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_30_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_30.t2 --TFHE
cp ./src/test/resources/fibonacci/fibonacci_30.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_30_TFHE.log
cd ../..


# 40 Iterations

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_40.t2 --HELIB \
  --config src/test/resources/fibonacci/configs/helib-bfv-int-128-40.config
cp ./src/test/resources/fibonacci/fibonacci_40.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_40_HElib.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_40.t2 --LATTIGO \
  --config src/test/resources/fibonacci/configs/lattigo-bfv-int-128-40.config
cp ./src/test/resources/fibonacci/fibonacci_40.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_40_Lattigo.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_40.t2 --PALISADE \
  --config src/test/resources/fibonacci/configs/palisade-bfv-int-128-40.config
cp ./src/test/resources/fibonacci/fibonacci_40.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_40_PALISADE.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_40.t2 --SEAL \
  --config src/test/resources/fibonacci/configs/seal-bfv-int-128-40.config
cp ./src/test/resources/fibonacci/fibonacci_40.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_40_SEAL.log
cd ../..

java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_40.t2 --TFHE
cp ./src/test/resources/fibonacci/fibonacci_40.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/fibonacci/fibonacci_40_TFHE.log
cd ../..

