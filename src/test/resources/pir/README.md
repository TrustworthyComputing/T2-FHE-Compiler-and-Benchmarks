# PIR with 128-bit security
## Integer, Floating Point, and Binary Domain Benchmark

### HElib
* `Integer (64)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_64_int.t2 --HELIB \
  --config src/test/resources/pir/configs/helib-bgv-128-int-64.config
  ```
* `Integer (128)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_128_int.t2 --HELIB \
  --config src/test/resources/pir/configs/helib-bgv-128-int-128.config
  ```
* `Integer (256)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_256_int.t2 --HELIB \
  --config src/test/resources/pir/configs/helib-bgv-128-int-256.config
  ```
* `Floating Point (64)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_64_fp.t2 --HELIB \
  --config src/test/resources/pir/configs/helib-ckks-128-fp.config
  ```
* `Floating Point (128)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_128_fp.t2 --HELIB \
  --config src/test/resources/pir/configs/helib-ckks-128-fp.config
  ```
* `Floating Point (256)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_256_fp.t2 --HELIB \
  --config src/test/resources/pir/configs/helib-ckks-128-fp.config
  ```
* `Binary (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_4_bin.t2 --HELIB --w 5 \
  --config src/test/resources/pir/configs/helib-bgv-128-bin.config
  ```
* `Binary (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_8_bin.t2 --HELIB --w 5 \
  --config src/test/resources/pir/configs/helib-bgv-128-bin.config
  ```
* `Binary (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_16_bin.t2 --HELIB --w 5 \
  --config src/test/resources/pir/configs/helib-bgv-128-bin.config
  ```
  
### Lattigo
* `Integer (64)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_64_int.t2 --Lattigo \
  --config src/test/resources/pir/configs/lattigo-bfv-128-int.config
  ```
* `Integer (128)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_128_int.t2 --Lattigo \
  --config src/test/resources/pir/configs/lattigo-bfv-128-int.config
  ```
* `Integer (256)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_256_int.t2 --Lattigo \
  --config src/test/resources/pir/configs/lattigo-bfv-128-int.config
  ```
* `Floating Point (64)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_64_fp.t2 --Lattigo \
  --config src/test/resources/pir/configs/lattigo-ckks-128-fp.config
  ```
* `Floating Point (128)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_128_fp.t2 --Lattigo \
  --config src/test/resources/pir/configs/lattigo-ckks-128-fp.config
  ```
* `Floating Point (256)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_256_fp.t2 --Lattigo \
  --config src/test/resources/pir/configs/lattigo-ckks-128-fp.config
  ```
* `Binary (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_4_bin.t2 --Lattigo --w 5 \
  --config src/test/resources/pir/configs/lattigo-bfv-128-bin.config
  ```
* `Binary (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_8_bin.t2 --Lattigo --w 5 \
  --config src/test/resources/pir/configs/lattigo-bfv-128-bin-big.config
  ```
* `Binary (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_16_bin.t2 --Lattigo --w 5 \
  --config src/test/resources/pir/configs/lattigo-bfv-128-bin-big.config
  ```

### PALISADE
* `Integer (64)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_64_int.t2 --PALISADE \
  --config src/test/resources/pir/configs/palisade-bfv-128-int.config
  ```
* `Integer (128)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_128_int.t2 --PALISADE \
  --config src/test/resources/pir/configs/palisade-bfv-128-int.config
  ```
* `Integer (256)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_256_int.t2 --PALISADE \
  --config src/test/resources/pir/configs/palisade-bfv-128-int.config
  ```
* `Floating Point (64)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_64_fp.t2 --PALISADE \
  --config src/test/resources/pir/configs/palisade-ckks-128-fp.config
  ```
* `Floating Point (128)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_128_fp.t2 --PALISADE \
  --config src/test/resources/pir/configs/palisade-ckks-128-fp.config
  ```
* `Floating Point (256)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_256_fp.t2 --PALISADE \
  --config src/test/resources/pir/configs/palisade-ckks-128-fp.config
  ```
* `Binary (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_4_bin.t2 --PALISADE --w 5 \
  --config src/test/resources/pir/configs/palisade-bfv-128-bin.config
  ```
* `Binary (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_8_bin.t2 --PALISADE --w 5 \
  --config src/test/resources/pir/configs/palisade-bfv-128-bin-8.config
  ```
* `Binary (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_16_bin.t2 --PALISADE --w 5 \
  --config src/test/resources/pir/configs/palisade-bfv-128-bin-16.config
  ```


### SEAL
* `Integer (64)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_64_int.t2 --SEAL \
  --config src/test/resources/pir/configs/seal-bfv-128-int.config
  ```
* `Integer (128)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_128_int.t2 --SEAL \
  --config src/test/resources/pir/configs/seal-bfv-128-int.config
  ```
* `Integer (256)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_256_int.t2 --SEAL \
  --config src/test/resources/pir/configs/seal-bfv-128-int.config
  ```
* `Floating Point (64)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_64_fp.t2 --SEAL \
  --config src/test/resources/pir/configs/seal-ckks-128-fp.config
  ```
* `Floating Point (128)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_128_fp.t2 --SEAL \
  --config src/test/resources/pir/configs/seal-ckks-128-fp.config
  ```
* `Floating Point (256)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_256_fp.t2 --SEAL \
  --config src/test/resources/pir/configs/seal-ckks-128-fp.config
  ```
* `Binary (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_4_bin.t2 --SEAL --w 5 \
  --config src/test/resources/pir/configs/seal-bfv-128-bin.config
  ```
* `Binary (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_8_bin.t2 --SEAL --w 5 \
  --config src/test/resources/pir/configs/seal-bfv-128-bin.config
  ```


### TFHE
* `Integer (64)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_64_int.t2 --TFHE --w 5
  ```
* `Integer (128)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_128_int.t2 --TFHE --w 5
  ```
* `Integer (256)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_256_int.t2 --TFHE --w 5
  ```
* `Binary (4)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_4_bin.t2 --TFHE --w 5 
  ```
* `Binary (8)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_8_bin.t2 --TFHE --w 5 
  ```
* `Binary (16)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/pir/pir_16_bin.t2 --TFHE --w 5 
  ```

