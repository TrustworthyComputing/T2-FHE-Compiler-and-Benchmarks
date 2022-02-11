#!/bin/bash

set -exo pipefail

echo "Testing Lattigo"

export PATH=$PATH:/usr/local/go/bin
mkdir -p ./src/Lattigo/compiled
cd ./src/Lattigo
go mod tidy
cd ../..

# Integer Domain Tests
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arithmetic.t2 --Lattigo
cp ./src/test/resources/tests/arithmetic.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/arithmetic_Lattigo.log
diff <(head -n -1 ../test/resources/tests/arithmetic_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arithmetic.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/batching.t2 --Lattigo
cp ./src/test/resources/tests/batching.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/batching_Lattigo.log
diff <(head -n -1 ../test/resources/tests/batching_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/batching.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/cmp.t2 --Lattigo --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/lattigo_insecure_int_cmp.config
cp ./src/test/resources/tests/cmp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/cmp_Lattigo.log
diff <(head -n -1 ../test/resources/tests/cmp_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/cmp.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/ternary.t2 --Lattigo --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/lattigo_insecure_int_cmp.config
cp ./src/test/resources/tests/ternary.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/ternary_Lattigo.log
diff <(head -n -1 ../test/resources/tests/ternary_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/ternary.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arrays.t2 --Lattigo --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/lattigo-128-more-levels.config
cp ./src/test/resources/tests/arrays.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/arrays_Lattigo.log
diff <(head -n -1 ../test/resources/tests/arrays_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arrays.res
cd ../..

# Binary Domain Tests
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/shift.t2 --Lattigo --w 6
cp ./src/test/resources/tests/shift.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/shift_Lattigo.log
diff <(head -n -1 ../test/resources/tests/shift_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/shift.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/batching.t2 --Lattigo --w 5
cp ./src/test/resources/tests/batching.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/batching_w5_Lattigo.log
diff <(head -n -1 ../test/resources/tests/batching_w5_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/batching_w5.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/cmp.t2 --Lattigo --w 5 --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/lattigo_insecure_int_cmp.config
cp ./src/test/resources/tests/cmp.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/cmp_w5_Lattigo.log
diff <(head -n -1 ../test/resources/tests/cmp_w5_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/cmp_w5.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/ternary.t2 --Lattigo --w 6 --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/lattigo-128-more-levels.config
cp ./src/test/resources/tests/ternary.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/ternary_w6_Lattigo.log
diff <(head -n -1 ../test/resources/tests/ternary_w6_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/ternary_w6.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arrays.t2 --Lattigo --w 5 --config src/main/java/org/twc/terminator/t2dsl_compiler/configs/lattigo-128-more-levels.config
cp ./src/test/resources/tests/arrays.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/arrays_w5_Lattigo.log
diff <(head -n -1 ../test/resources/tests/arrays_w5_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/arrays_w5.res
cd ../..

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/bitwise.t2 --LATTIGO --w 6
cp ./src/test/resources/tests/bitwise.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/bitwise_Lattigo.log
diff <(head -n -1 ../test/resources/tests/bitwise_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/bitwise_w6.res
cd ../..

# Floating Point Domain Tests

java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/ckks_test.t2 --LATTIGO
cp ./src/test/resources/tests/ckks_test.go ./src/Lattigo/compiled/test.go
cd ./src/Lattigo
make
./bin/test.out > ../test/resources/tests/ckks_test_Lattigo.log
diff <(head -n -1 ../test/resources/tests/ckks_test_Lattigo.log | awk '{$1=$1};1' | cut -d ' ' -f 3-) ../test/resources/tests/ckks_test.res
cd ../..
