# LR Inference Benchmark
## Integer & Floating-Point Domain Benchmark

### HElib
* `Integer (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a4.t2 --HELIB \
  --config src/test/resources/logistic_regression/configs/helib-bgv-128-int.config
  ```
* `Integer (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a8.t2 --HELIB \
  --config src/test/resources/logistic_regression/configs/helib-bgv-128-int.config
  ```
* `Integer (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a16.t2 --HELIB \
  --config src/test/resources/logistic_regression/configs/helib-bgv-128-int.config
  ```
* `Floating Point (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a4_fp.t2 --HELIB \
  --config src/test/resources/logistic_regression/configs/helib-ckks-128-fp.config
  ```
* `Floating Point (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a8_fp.t2 --HELIB \
  --config src/test/resources/logistic_regression/configs/helib-ckks-128-fp.config
  ```
* `Floating Point (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a16_fp.t2 --HELIB \
  --config src/test/resources/logistic_regression/configs/helib-ckks-128-fp.config
  ```

### Lattigo
* `Integer (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a4.t2 --LATTIGO \
  --config src/test/resources/logistic_regression/configs/lattigo-bfv-16k-128-int.config
  ```
* `Integer (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a8.t2 --LATTIGO \
  --config src/test/resources/logistic_regression/configs/lattigo-bfv-16k-128-int.config
  ```
* `Integer (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a16.t2 --LATTIGO \
  --config src/test/resources/logistic_regression/configs/lattigo-bfv-16k-128-int.config
  ```
* `Floating Point (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a4_fp.t2 --LATTIGO \
  --config src/test/resources/logistic_regression/configs/lattigo-ckks-16k-128-fp.config
  ```
* `Floating Point (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a8_fp.t2 --LATTIGO \
  --config src/test/resources/logistic_regression/configs/lattigo-ckks-16k-128-fp.config
  ```
* `Floating Point (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a16_fp.t2 --LATTIGO \
  --config src/test/resources/logistic_regression/configs/lattigo-ckks-16k-128-fp.config
  ```

### PALISADE
* `Integer (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a4.t2 --PALISADE \
  --config src/test/resources/logistic_regression/configs/palisade-bfv-16k-128-int.config
  ```
* `Integer (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a8.t2 --PALISADE \
  --config src/test/resources/logistic_regression/configs/palisade-bfv-16k-128-int.config
  ```
* `Integer (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a16.t2 --PALISADE \
  --config src/test/resources/logistic_regression/configs/palisade-bfv-16k-128-int.config
  ```
* `Floating Point (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a4_fp.t2 --PALISADE \
  --config src/test/resources/logistic_regression/configs/palisade-ckks-16k-128-fp.config
  ```
* `Floating Point (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a8_fp.t2 --PALISADE \
  --config src/test/resources/logistic_regression/configs/palisade-ckks-16k-128-fp.config
  ```
* `Floating Point (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a16_fp.t2 --PALISADE \
  --config src/test/resources/logistic_regression/configs/palisade-ckks-16k-128-fp.config
  ```

### SEAL
* `Integer (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a4.t2 --SEAL \
  --config src/test/resources/logistic_regression/configs/seal-bfv-16k-128-int.config
  ```
* `Integer (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a8.t2 --SEAL \
  --config src/test/resources/logistic_regression/configs/seal-bfv-16k-128-int.config
  ```
* `Integer (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a16.t2 --SEAL \
  --config src/test/resources/logistic_regression/configs/seal-bfv-16k-128-int.config
  ```
* `Floating Point (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a4_fp.t2 --SEAL \
  --config src/test/resources/logistic_regression/configs/seal-ckks-16k-128-fp.config
  ```
* `Floating Point (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a8_fp.t2 --SEAL \
  --config src/test/resources/logistic_regression/configs/seal-ckks-16k-128-fp.config
  ```
* `Floating Point (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a16_fp.t2 --SEAL \
  --config src/test/resources/logistic_regression/configs/seal-ckks-16k-128-fp.config
  ```

### TFHE
* `Integer (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a4.t2 --TFHE --w 16
  ```
* `Integer (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a8.t2 --TFHE --w 16
  ```
* `Integer (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/logistic_regression/logistic_regression_a16.t2 --TFHE --w 16
  ```
