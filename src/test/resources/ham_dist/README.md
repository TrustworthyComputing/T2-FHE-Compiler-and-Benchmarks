# Manhattan Distance with 128-bit security
## Binary and Integer Domain Benchmark

### HElib (Binary)
* `Vector size 4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --HELIB --w 4 \
  --config src/test/resources/ham_dist/configs/helib-bfv-128.config
  ```
* `Vector size 8`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v8.t2 --HELIB --w 4 \
  --config src/test/resources/ham_dist/configs/helib-bfv-128.config
  ```

### Lattigo (Binary)
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

### PALISADE (Binary)
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

### SEAL (Binary)
* `Vector size 4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --SEAL --w 4 \
  --config src/test/resources/ham_dist/configs/seal-bfv-128.config
  ```

### TFHE (Binary)
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

### HElib (Integer)
* `Vector size 4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --HELIB \
  --config src/test/resources/ham_dist/configs/helib-bgv-128-v4-int.config
  ```
* `Vector size 8`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v8.t2 --HELIB \
  --config src/test/resources/ham_dist/configs/helib-bgv-128-v4-int.config
  ``

### Lattigo (Integer)
* `Vector size 4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --Lattigo \
  --config src/test/resources/ham_dist/configs/lattigo-bfv-128-int.config
  ```
* `Vector size 8`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v8.t2 --Lattigo \
  --config src/test/resources/ham_dist/configs/lattigo-bfv-128-int.config
  ```

### PALISADE (Integer)
* `Vector size 4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --PALISADE \
  --config src/test/resources/ham_dist/configs/palisade-bfv-128-v4-int.config
  ```
* `Vector size 8`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v8.t2 --PALISADE \
  --config src/test/resources/ham_dist/configs/palisade-bfv-128-v4-int.config
  ``` 

### SEAL (Integer)
* `Vector size 4`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v4.t2 --SEAL \
  --config src/test/resources/ham_dist/configs/seal-bfv-128-v4.config
  ```
* `Vector size 8`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/ham_dist/hamming_distance_v8.t2 --SEAL \
  --config src/test/resources/ham_dist/configs/seal-bfv-128-v4.config
  ```