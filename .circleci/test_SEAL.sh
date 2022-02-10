#!/bin/bash

set -exo pipefail

echo "Testing SEAL"

mkdir -p ./src/SEAL/compiled

# Integer Domain Tests
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arithmetic.t2 --SEAL
cp ./src/test/resources/tests/arithmetic.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
cmake .
make
./bin/test.out > ../test/resources/tests/arithmetic_SEAL.log
diff <(head -n -1 ../test/resources/tests/arithmetic_SEAL.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arithmetic.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/batching.t2 --SEAL
cp ./src/test/resources/tests/batching.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/batching_SEAL.log
diff <(head -n -1 ../test/resources/tests/batching_SEAL.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/batching.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/cmp.t2 --SEAL --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/seal-bfv-comparisons.config
cp ./src/test/resources/tests/cmp.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/cmp_SEAL.log
diff <(head -n -1 ../test/resources/tests/cmp_SEAL.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/cmp.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/ternary.t2 --SEAL --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/seal-bfv-comparisons.config
cp ./src/test/resources/tests/ternary.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/ternary_SEAL.log
diff <(head -n -1 ../test/resources/tests/ternary_SEAL.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/ternary.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arrays.t2 --SEAL --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/seal-bfv-bin-comparisons-insecure.config
cp ./src/test/resources/tests/arrays.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/arrays_SEAL.log
diff <(head -n -1 ../test/resources/tests/arrays_SEAL.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arrays.res
cd ../..

# Binary Domain Tests
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/shift.t2 --SEAL --w 6
cp ./src/test/resources/tests/shift.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/shift_SEAL.log
diff <(head -n -1 ../test/resources/tests/shift_SEAL.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/shift.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/batching.t2 --SEAL --w 5
cp ./src/test/resources/tests/batching.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/batching_w5_SEAL.log
diff <(head -n -1 ../test/resources/tests/batching_w5_SEAL.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/batching_w5.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/cmp.t2 --SEAL --w 5 --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/seal-bfv-bin-comparisons-insecure.config
cp ./src/test/resources/tests/cmp.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/cmp_w5_SEAL.log
diff <(head -n -1 ../test/resources/tests/cmp_w5_SEAL.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/cmp_w5.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/ternary.t2 --SEAL --w 6
cp ./src/test/resources/tests/ternary.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/ternary_w6_SEAL.log
diff <(head -n -1 ../test/resources/tests/ternary_w6_SEAL.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/ternary_w6.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arrays.t2 --SEAL --w 5
cp ./src/test/resources/tests/arrays.cpp ./src/SEAL/compiled/test.cpp
cd ./src/SEAL
make
./bin/test.out > ../test/resources/tests/arrays_w5_SEAL.log
diff <(head -n -1 ../test/resources/tests/arrays_w5_SEAL.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arrays_w5.res
cd ../..
