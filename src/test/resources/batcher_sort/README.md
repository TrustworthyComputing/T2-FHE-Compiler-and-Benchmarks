# Batcher Sorting Network Benchmark
## Binary Benchmark

### HElib
* `Binary Domain (4 elements)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/batcher_sort/batcher_sort_4.t2 --HELIB --w 4 \
  --config src/test/resources/batcher_sort/configs/helib-bgv-boot.config
  ```

* `Binary Domain (8 elements)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/batcher_sort/batcher_sort_8.t2 --HELIB --w 4 \
  --config src/test/resources/batcher_sort/configs/helib-bgv-boot.config
  ```

* `Binary Domain (16 elements)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/batcher_sort/batcher_sort_16.t2 --HELIB --w 4 \
  --config src/test/resources/batcher_sort/configs/helib-bgv-boot.config
  ```

### TFHE
* `Binary Domain (4 elements)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/batcher_sort/batcher_sort_4.t2 --TFHE --w 4
  ```

* `Binary Domain (8 elements)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/batcher_sort/batcher_sort_8.t2 --TFHE --w 4
  ```

* `Binary Domain (16 elements)`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/batcher_sort/batcher_sort_16.t2 --TFHE --w 4
  ```