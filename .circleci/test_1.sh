#!/bin/bash

set -exo pipefail

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arrays.t2 --PALISADE --w 5 --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/palisade-bgv-bin-10-levels.config
cp ./src/test/resources/tests/arrays.cpp ./src/PALISADE/compiled/test.cpp
cd ./src/PALISADE
make
./bin/test.out > ../test/resources/tests/arrays_w5_PALISADE.log
diff <(head -n -1 ../test/resources/tests/arrays_w5_PALISADE.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arrays_w5.res
cd ../..
