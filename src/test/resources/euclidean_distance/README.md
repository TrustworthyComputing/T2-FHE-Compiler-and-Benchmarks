# Euclidean Distance with 128-bit security
## Integer and Floating Point Domain Benchmark

### HElib
* `Vector size 64 Integer`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v64.t2 --HELIB  \
  --config src/test/resources/euclidean_distance/configs/helib-bfv-128.config
  ```
* `Vector size 128 Integer`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v128.t2 --HELIB  \
  --config src/test/resources/euclidean_distance/configs/helib-bfv-128.config
  ```
* `Vector size 64 Floating Point`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v64_ckks.t2 --HELIB  \
  --config src/test/resources/euclidean_distance/configs/helib-ckks-128.config
  ```
* `Vector size 128 Floating Point`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v128_ckks.t2 --HELIB  \
  --config src/test/resources/euclidean_distance/configs/helib-ckks-128.config
  ```

### Lattigo
* `Vector size 64 Integer`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v64.t2 --Lattigo  \
  --config src/test/resources/euclidean_distance/configs/lattigo-bfv-128.config
  ```
* `Vector size 128 Integer`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v128.t2 --Lattigo  \
  --config src/test/resources/euclidean_distance/configs/lattigo-bfv-128.config
  ```
* `Vector size 64 Floating Point`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v64_ckks.t2 --Lattigo  \
  --config src/test/resources/euclidean_distance/configs/lattigo-ckks-128.config
  ```
* `Vector size 128 Floating Point`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v128_ckks.t2 --Lattigo  \
  --config src/test/resources/euclidean_distance/configs/lattigo-ckks-128.config
  ```


### PALISADE
* `Vector size 64 Integer`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v64.t2 --PALISADE  \
  --config src/test/resources/euclidean_distance/configs/palisade-bfv-128.config
  ```
* `Vector size 128 Integer`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v128.t2 --PALISADE  \
  --config src/test/resources/euclidean_distance/configs/palisade-bfv-128.config
  ```
* `Vector size 64 Floating Point`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v64_ckks.t2 --PALISADE  \
  --config src/test/resources/euclidean_distance/configs/palisade-ckks-128.config
  ```
* `Vector size 128 Floating Point`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v128_ckks.t2 --PALISADE  \
  --config src/test/resources/euclidean_distance/configs/palisade-ckks-128.config
  ```


### SEAL
* `Vector size 64 Integer`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v64.t2 --SEAL  \
  --config src/test/resources/euclidean_distance/configs/seal-bfv-128.config
  ```
* `Vector size 128 Integer`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v128.t2 --SEAL  \
  --config src/test/resources/euclidean_distance/configs/seal-bfv-128.config
  ```
* `Vector size 64 Floating Point`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v64_ckks.t2 --SEAL  \
  --config src/test/resources/euclidean_distance/configs/seal-ckks-128.config
  ```
* `Vector size 128 Floating Point`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v128_ckks.t2 --SEAL  \
  --config src/test/resources/euclidean_distance/configs/seal-ckks-128.config
  ```


### TFHE
* `Vector size 64 Integer`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v64.t2 --TFHE
  ```
* `Vector size 128 Integer`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/euclidean_distance/euclidean_distance_v128.t2 --TFHE
  ```
