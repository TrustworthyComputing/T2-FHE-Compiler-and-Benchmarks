#!/bin/bash

set -exo pipefail

echo "Testing HElib"

mkdir -p ./src/HElib/compiled
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/helib_install/helib_pack/lib

# Integer Domain Tests
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arithmetic.t2 --HElib
cp ./src/test/resources/tests/arithmetic.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/arithmetic_HElib.log
diff <(head -n -1 ../test/resources/tests/arithmetic_HElib.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arithmetic.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/batching.t2 --HElib
cp ./src/test/resources/tests/batching.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/batching_HElib.log
diff <(head -n -1 ../test/resources/tests/batching_HElib.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/batching.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/cmp.t2 --HElib --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/helib-bfv-cmp-int-insecure.config
cp ./src/test/resources/tests/cmp.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/cmp_HElib.log
diff <(head -n -1 ../test/resources/tests/cmp_HElib.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/cmp.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/ternary.t2 --HElib --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/helib-bfv-cmp-int-insecure.config
cp ./src/test/resources/tests/ternary.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/ternary_HElib.log
diff <(head -n -1 ../test/resources/tests/ternary_HElib.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/ternary.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arrays.t2 --HElib
cp ./src/test/resources/tests/arrays.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/arrays_HElib.log
diff <(head -n -1 ../test/resources/tests/arrays_HElib.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arrays.res
cd ../..

# Binary Domain Tests
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/shift.t2 --HElib --w 6
cp ./src/test/resources/tests/shift.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/shift_HElib.log
diff <(head -n -1 ../test/resources/tests/shift_HElib.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/shift.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/batching.t2 --HElib --w 5
cp ./src/test/resources/tests/batching.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/batching_w5_HElib.log
diff <(head -n -1 ../test/resources/tests/batching_w5_HElib.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/batching_w5.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/cmp.t2 --HElib --w 5
cp ./src/test/resources/tests/cmp.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/cmp_w5_HElib.log
diff <(head -n -1 ../test/resources/tests/cmp_w5_HElib.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/cmp_w5.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/ternary.t2 --HElib --w 6
cp ./src/test/resources/tests/ternary.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/ternary_w6_HElib.log
diff <(head -n -1 ../test/resources/tests/ternary_w6_HElib.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/ternary_w6.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arrays.t2 --HElib --w 5
cp ./src/test/resources/tests/arrays.cpp ./src/HElib/compiled/test.cpp
cd ./src/HElib
make
./bin/test.out > ../test/resources/tests/arrays_w5_HElib.log
diff <(head -n -1 ../test/resources/tests/arrays_w5_HElib.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arrays_w5.res
cd ../..
