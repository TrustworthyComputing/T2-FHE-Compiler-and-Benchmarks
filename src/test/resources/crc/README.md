# CRC-8 Benchmark
## Binary Benchmark

### HElib
* `Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --HELIB --w 8 \
  --config src/test/resources/crc/configs/helib-bfv-crc8-128.config
  ```

### Lattigo
* `Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --LATTIGO --w 8 \
  --config src/tests/resources/crc/configs/lattigo-bfv-crc8-128.config
  ```

### PALISADE
* `Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --PALISADE --w 8 \
  --config src/test/resources/crc/configs/palisade-bfv-crc8-128.config
  ```

### SEAL
* `Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --SEAL --w 8 \
  --config src/test/resources/crc/configs/seal-bfv-crc8-128.config
  ```

### TFHE
* `Binary Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/crc/crc8.t2 --TFHE --w 8
  ```
