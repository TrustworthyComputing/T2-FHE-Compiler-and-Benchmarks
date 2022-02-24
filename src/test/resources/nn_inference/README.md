# Neural Network Inference with 128-bit security
## Integer and Floating Point Domain Benchmark

### HElib
* `50 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_50.t2 --HELIB \
  --config src/test/resources/nn_inference/configs/helib-bgv-128.config
  ```
* `100 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_100.t2 --HELIB \
  --config src/test/resources/nn_inference/configs/helib-bgv-128.config
  ```
* `150 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_150.t2 --HELIB \
  --config src/test/resources/nn_inference/configs/helib-bgv-128.config
  ```
* `50 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_50_fp.t2 --HELIB \
  --config src/test/resources/nn_inference/configs/helib-ckks-128.config
  ```
* `100 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_100_fp.t2 --HELIB \
  --config src/test/resources/nn_inference/configs/helib-ckks-128.config
  ```
* `150 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_150_fp.t2 --HELIB \
  --config src/test/resources/nn_inference/configs/helib-ckks-128.config
  ```
  
### Lattigo
* `50 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_50.t2 --LATTIGO \
  --config src/test/resources/nn_inference/configs/lattigo-bfv-128.config
  ```
* `100 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_100.t2 --LATTIGO \
  --config src/test/resources/nn_inference/configs/lattigo-bfv-128.config
  ```
* `150 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_150.t2 --LATTIGO \
  --config src/test/resources/nn_inference/configs/lattigo-bfv-128.config
  ```
* `50 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_50_fp.t2 --LATTIGO \
  --config src/test/resources/nn_inference/configs/lattigo-ckks-128.config
  ```
* `100 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_100_fp.t2 --LATTIGO \
  --config src/test/resources/nn_inference/configs/lattigo-ckks-128.config
  ```
* `150 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_150_fp.t2 --LATTIGO \
  --config src/test/resources/nn_inference/configs/lattigo-ckks-128.config
  ```

### PALISADE
* `50 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_50.t2 --PALISADE \
  --config src/test/resources/nn_inference/configs/palisade-bfv-128.config
  ```
* `100 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_100.t2 --PALISADE \
  --config src/test/resources/nn_inference/configs/palisade-bfv-128.config
  ```
* `150 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_150.t2 --PALISADE \
  --config src/test/resources/nn_inference/configs/palisade-bfv-128.config
  ```
* `50 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_50_fp.t2 --PALISADE \
  --config src/test/resources/nn_inference/configs/palisade-ckks-128.config
  ```
* `100 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_100_fp.t2 --PALISADE \
  --config src/test/resources/nn_inference/configs/palisade-ckks-128.config
  ```
* `150 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_150_fp.t2 --PALISADE \
  --config src/test/resources/nn_inference/configs/palisade-ckks-128.config
  ```

### SEAL
* `50 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_50.t2 --SEAL \
  --config src/test/resources/nn_inference/configs/seal-bfv-128.config
  ```
* `100 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_100.t2 --SEAL \
  --config src/test/resources/nn_inference/configs/seal-bfv-128.config
  ```
* `150 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_150.t2 --SEAL \
  --config src/test/resources/nn_inference/configs/seal-bfv-128.config
  ```
* `50 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_50_fp.t2 --SEAL \
  --config src/test/resources/nn_inference/configs/seal-ckks-128.config
  ```
* `100 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_100_fp.t2 --SEAL \
  --config src/test/resources/nn_inference/configs/seal-ckks-128.config
  ```
* `150 Active Neurons (Floating Point)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_150_fp.t2 --SEAL \
  --config src/test/resources/nn_inference/configs/seal-ckks-128.config
  ```
  
### TFHE
* `50 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_50.t2 --TFHE -w 16
  ```
* `100 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_100.t2 --TFHE -w 16
  ```
* `150 Active Neurons (Integer)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/nn_inference/nn_150.t2 --TFHE -w 16
  ```