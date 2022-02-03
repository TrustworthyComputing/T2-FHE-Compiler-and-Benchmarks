# Chi^2 Benchmark
## Integer & Floating-Point Domain Benchmark

### HElib
* `Integer Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --HELIB \
  --config src/test/resources/chi_squared/configs/helib-bfv-128.config
  ```
* `Floating-Point Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared_ckks.t2 --HELIB \
  --config src/test/resources/chi_squared/configs/helib-ckks-128.config

### Lattigo
* `Integer Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --LATTIGO
  ```
* `Floating-Point Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared_ckks.t2 --LATTIGO \
  --config src/test/resources/chi_squared/configs/lattigo-ckks-128.config

### PALISADE
* `Integer Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --PALISADE \
  --config src/test/resources/chi_squared/configs/palisade-bfv-128.config
  ```
* `Floating-Point Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared_ckks.t2 --PALISADE \
  --config src/test/resources/chi_squared/configs/palisade-ckks-128.config

### SEAL
* `Integer Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --SEAL \
  --config src/test/resources/chi_squared/configs/seal-bfv-128.config
  ```
* `Floating-Point Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared_ckks.t2 --SEAL \
  --config src/test/resources/chi_squared/configs/seal-ckks-256.config

### TFHE
* `Integer Domain`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/chi_squared/chi_squared.t2 --TFHE
  ```
