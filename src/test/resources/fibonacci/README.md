# Fibonacci Sequence with 128-bit security
## Integer Domain Benchmark

### HElib
* `20 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_20.t2 --HELIB \
  --config src/test/resources/fibonacci/configs/helib-bfv-int-128-20.config
  ```
* `30 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_30.t2 --HELIB \
  --config src/test/resources/fibonacci/configs/helib-bfv-int-128-30.config
  ```
* `40 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_40.t2 --HELIB \
  --config src/test/resources/fibonacci/configs/helib-bfv-int-128-40.config
  ```

### Lattigo
* `20 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_20.t2 --LATTIGO \
  --config src/test/resources/fibonacci/configs/lattigo-bfv-int-128-20.config
  ```
* `30 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_30.t2 --LATTIGO \
  --config src/test/resources/fibonacci/configs/lattigo-bfv-int-128-30.config
  ```
* `40 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_40.t2 --LATTIGO \
  --config src/test/resources/fibonacci/configs/lattigo-bfv-int-128-40.config
  ```

### PALISADE
* `20 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_20.t2 --PALISADE \
  --config src/test/resources/fibonacci/configs/palisade-bfv-int-128-20.config
  ```
* `30 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_30.t2 --PALISADE \
  --config src/test/resources/fibonacci/configs/palisade-bfv-int-128-30.config
  ```
* `40 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_40.t2 --PALISADE \
  --config src/test/resources/fibonacci/configs/palisade-bfv-int-128-40.config
  ```

### SEAL
* `20 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_20.t2 --SEAL \
  --config src/test/resources/fibonacci/configs/seal-bfv-int-128-20.config
  ```
* `30 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_30.t2 --SEAL \
  --config src/test/resources/fibonacci/configs/seal-bfv-int-128-30.config
  ```
* `40 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_40.t2 --SEAL \
  --config src/test/resources/fibonacci/configs/seal-bfv-int-128-40.config
  ```

### TFHE
* `20 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_20.t2 --TFHE
  ```
* `30 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_30.t2 --TFHE
  ```
* `40 Iterations`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/fibonacci/fibonacci_40.t2 --TFHE
  ```
