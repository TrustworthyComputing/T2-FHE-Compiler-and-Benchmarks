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

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/batching.t2 --TFHE --w 5 --printbin
cp ./src/test/resources/tests/batching.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/tests/batching_w5_TFHE.log
diff <(head -n -1 ../test/resources/tests/batching_w5_TFHE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/batching_w5.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/cmp.t2 --TFHE --w 5
cp ./src/test/resources/tests/cmp.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/tests/cmp_TFHE.log
diff <(head -n -1 ../test/resources/tests/cmp_TFHE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/cmp.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/cmp.t2 --TFHE --w 5 --printbin
cp ./src/test/resources/tests/cmp.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/tests/cmp_w5_TFHE.log
diff <(head -n -1 ../test/resources/tests/cmp_w5_TFHE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/cmp_w5.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/ternary.t2 --TFHE --w 6
cp ./src/test/resources/tests/ternary.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/tests/ternary_TFHE.log
diff <(head -n -1 ../test/resources/tests/ternary_TFHE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/ternary.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arrays.t2 --TFHE --w 5
cp ./src/test/resources/tests/arrays.cpp ./src/TFHE/compiled/test.cpp
cd ./src/TFHE
make
./bin/test.out > ../test/resources/tests/arrays_TFHE.log
diff <(head -n -1 ../test/resources/tests/arrays_TFHE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arrays.res
cd ../..
