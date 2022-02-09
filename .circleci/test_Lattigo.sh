#!/bin/bash

set -exo pipefail

echo "Testing Lattigo"

export PATH=$PATH:/usr/local/go/bin
mkdir -p ./src/Lattigo/compiled
cd ./src/Lattigo
if [ ! -d "go.mod" ] ; then
    go mod init Lattigo
    go mod tidy
fi
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
