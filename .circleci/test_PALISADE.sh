#!/bin/bash

set -exo pipefail

echo "Testing PALISADE"

mkdir -p ./src/PALISADE/compiled

# Integer Domain Tests
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arithmetic.t2 --PALISADE --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/palisade-bgv-int-2-levels.config
cp ./src/test/resources/tests/arithmetic.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
cmake .
make
./bin/test.out > ../test/resources/tests/arithmetic_PALISADE.log
diff <(head -n -1 ../test/resources/tests/arithmetic_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arithmetic.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/batching.t2 --PALISADE -config src/main/java/org/twc/terminator/t2dsl_compiler/configs/palisade-bgv-int-2-levels.config
cp ./src/test/resources/tests/batching.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/batching_PALISADE.log
diff <(head -n -1 ../test/resources/tests/batching_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/batching.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/ternary.t2 --PALISADE --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/palisade-bgv-int-20-levels.config
cp ./src/test/resources/tests/ternary.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/ternary_PALISADE.log
diff <(head -n -1 ../test/resources/tests/ternary_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/ternary.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arrays.t2 --PALISADE --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/palisade-bgv-int-2-levels.config
cp ./src/test/resources/tests/arrays.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/arrays_PALISADE.log
diff <(head -n -1 ../test/resources/tests/arrays_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arrays.res
cd ../..

# Binary Domain Tests
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/shift.t2 --PALISADE --w 6 --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/palisade-bgv-bin-5-levels.config
cp ./src/test/resources/tests/shift.cpp ./src/PALISADE/compiled/test.cpp 
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/shift_PALISADE.log
diff <(head -n -1 ../test/resources/tests/shift_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/shift.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/batching.t2 --PALISADE --w 5 --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/palisade-bgv-bin-10-levels.config
cp ./src/test/resources/tests/batching.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/batching_w5_PALISADE.log
diff <(head -n -1 ../test/resources/tests/batching_w5_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/batching_w5.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/ternary.t2 --PALISADE --w 6 --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/palisade-bgv-bin-10-levels.config
cp ./src/test/resources/tests/ternary.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/ternary_w6_PALISADE.log
diff <(head -n -1 ../test/resources/tests/ternary_w6_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/ternary_w6.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arrays.t2 --PALISADE --w 5 --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/palisade-bgv-bin-10-levels.config
cp ./src/test/resources/tests/arrays.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/arrays_w5_PALISADE.log
diff <(head -n -1 ../test/resources/tests/arrays_w5_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arrays_w5.res
cd ../..
