#!/bin/bash

set -exo pipefail

echo "Testing TFHE"

mkdir -p ./src/TFHE/compiled

# Binary Domain Tests
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arithmetic.t2 --TFHE --w 8
cp ./src/test/resources/tests/arithmetic.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/tests/arithmetic_TFHE.log
diff <(head -n -1 ../test/resources/tests/arithmetic_TFHE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arithmetic.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/shift.t2 --TFHE --w 6 --printbin
cp ./src/test/resources/tests/shift.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/tests/shift_TFHE.log
diff <(head -n -1 ../test/resources/tests/shift_TFHE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/shift.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/batching.t2 --TFHE --w 6
cp ./src/test/resources/tests/batching.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/tests/batching_TFHE.log
diff <(head -n -1 ../test/resources/tests/batching_TFHE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/batching.res
cd ../..
