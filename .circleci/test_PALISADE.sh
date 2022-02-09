#!/bin/bash

set -exo pipefail

echo "Testing PALISADE"

mkdir -p ./src/PALISADE/compiled

# Integer Domain Tests
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arithmetic.t2 --PALISADE
cp ./src/test/resources/tests/arithmetic.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
cmake .
make
./bin/test.out > ../test/resources/tests/arithmetic_PALISADE.log
diff <(head -n -1 ../test/resources/tests/arithmetic_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arithmetic.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/batching.t2 --PALISADE
cp ./src/test/resources/tests/batching.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/batching_PALISADE.log
diff <(head -n -1 ../test/resources/tests/batching_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/batching.res
cd ../..

# Binary Domain Tests
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/shift.t2 --PALISADE --w 6
cp ./src/test/resources/tests/shift.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/shift_PALISADE.log
diff <(head -n -1 ../test/resources/tests/shift_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/shift.res
cd ../..
