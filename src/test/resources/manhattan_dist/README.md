# Manhattan Distance with 128-bit security
## Integer & Binary Domain Benchmark

### HElib
* `Vector size 4 -- Integer Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v4_int.t2 --HELIB \
  --config src/test/resources/manhattan_dist/configs/helib-bfv-int-128.config
  ```
* `Vector size 4 -- Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v4_bin.t2 --HELIB --w 4 \
  --config src/test/resources/manhattan_dist/configs/helib-bfv-bin-128.config

  ```
* `Vector size 8 -- Integer Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v8_int.t2 --HELIB \
  --config src/test/resources/manhattan_dist/configs/helib-bfv-int-128.config
  ```
* `Vector size 8 -- Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v8_bin.t2 --HELIB --w 4 \
  --config src/test/resources/manhattan_dist/configs/helib-bfv-bin-128.config
  ```

### Lattigo
* `Vector size 4 -- Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v4_bin.t2 --Lattigo --w 4 \
  --config src/test/resources/manhattan_dist/configs/lattigo-bfv-128.config
  ```
* `Vector size 8 -- Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v8_bin.t2 --Lattigo --w 4 \
  --config src/test/resources/manhattan_dist/configs/lattigo-bfv-128_v8.config
  ```

### PALISADE
* `Vector size 4 -- Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v4_bin.t2 --PALISADE --w 4 \
  --config src/test/resources/manhattan_dist/configs/palisade-bfv-bin-128.config
  ```
* `Vector size 8 -- Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v8_bin.t2 --PALISADE --w 4 \
  --config src/test/resources/manhattan_dist/configs/palisade-bfv-bin-128.config
  ``` 

### SEAL
* `Vector size 4 -- Integer Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v4_int.t2 --SEAL \
  --config src/test/resources/manhattan_dist/configs/seal-bfv-nonbatched-int-128.config
  ```
* `Vector size 8 -- Integer Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v8_int.t2 --SEAL \
  --config src/test/resources/manhattan_dist/configs/seal-bfv-nonbatched-int-128.config
  ```

### TFHE
* `Vector size 4 -- Integer Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v4_int.t2 --TFHE --w 4
  ```
* `Vector size 4 -- Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v4_bin.t2 --TFHE --w 4
  ```
* `Vector size 8 -- Integer Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v8_int.t2 --TFHE --w 4
  ```
* `Vector size 8 -- Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/manhattan_dist/manhattan_dist_v8_bin.t2 --TFHE --w 4
  ```
