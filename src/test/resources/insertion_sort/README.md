# Insertion Sort Benchmark
## Binary Benchmark

### HElib
* `4 Elements`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/insertion_sort/insertion_sort_4.t2 --HELIB --w 4 \
  --config src/test/resources/insertion_sort/configs/helib-bgv-128-bin.config
  ```
* `8 Elements`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/insertion_sort/insertion_sort_8.t2 --HELIB --bootstrap --w 4 \
  --config src/test/resources/insertion_sort/configs/helib-bgv-128-bin-boot-big.config
  ```
* `16 Elements`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/insertion_sort/insertion_sort_16.t2 --HELIB --bootstrap --w 4 \
  --config src/test/resources/insertion_sort/configs/helib-bgv-128-bin-boot-big.config
  ```

### TFHE
* `4 Elements`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/insertion_sort/insertion_sort_4.t2 --TFHE --w 4
  ```
* `8 Elements`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/insertion_sort/insertion_sort_8.t2 --TFHE --w 4
  ```
* `16 Elements`
  ```powershell
  java -jar target/terminator-compiler-1.0.jar \
  src/test/resources/insertion_sort/insertion_sort_16.t2 --TFHE --w 4
  ```
