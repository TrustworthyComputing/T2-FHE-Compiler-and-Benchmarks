# Matrix Multiplication with 128-bit security
## Integer Domain Benchmark

### HElib
* `4x4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_4x4.t2 --HELIB \
  --config src/test/resources/mmult/configs/helib-bfv-int-128-4x4.config
  ```
* `8x8`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_8x8.t2 --HELIB \
  --config src/test/resources/mmult/configs/helib-bfv-int-128-8x8.config
  ```
* `16x16`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_16x16.t2 --HELIB \
  --config src/test/resources/mmult/configs/helib-bfv-int-128-16x16.config
  ```

### Lattigo
* `4x4`, `8x8`, `16x16`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_4x4.t2 --LATTIGO \
  --config src/test/resources/mmult/configs/lattigo-bfv-int-128.config
  ```

### PALISADE
* `4x4`, `8x8`, `16x16`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_16x16.t2 --PALISADE \
  --config src/test/resources/mmult/configs/palisade-bfv-int-128.config
  ```

### SEAL
* `4x4`, `8x8`, `16x16`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_4x4.t2 --SEAL \
  --config src/test/resources/mmult/configs/seal-bfv-int-128.config
  ```

### TFHE
* `4x4`, `8x8`, `16x16`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/mmult/mmult_4x4.t2 --TFHE
  ```
