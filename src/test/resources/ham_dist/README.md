# Manhattan Distance with 128-bit security
## Binary Domain Benchmark

### HElib
* `Vector size 4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --HELIB --w 4 \
  --config src/test/resources/ham_dist/configs/helib-bfv-128.config

  ```
* `Vector size 8`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --HELIB --w 4 \
  --config src/test/resources/ham_dist/configs/helib-bfv-128.config
  ```

### Lattigo
* `Vector size 4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --Lattigo --w 4 \
  --config src/test/resources/ham_dist/configs/lattigo-bfv-128-v4.config
  ```
* `Vector size 8`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v8.t2 --Lattigo --w 4 \
  --config src/test/resources/ham_dist/configs/lattigo-bfv-128-v8.config
  ```

### PALISADE
* `Vector size 4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --PALISADE --w 4 \
  --config src/test/resources/ham_dist/configs/palisade-bfv-128-v4.config
  ```
* `Vector size 8`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v8.t2 --PALISADE --w 4 \
  --config src/test/resources/ham_dist/configs/palisade-bfv-128-v8.config
  ``` 

### SEAL
* `Vector size 4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --SEAL --w 4 \
  --config src/test/resources/ham_dist/configs/seal-bfv-128.config
  ```

### TFHE
* `Vector size 4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --TFHE --w 4 
  ```
* `Vector size 8`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v8.t2 --TFHE --w 4 
  ```
