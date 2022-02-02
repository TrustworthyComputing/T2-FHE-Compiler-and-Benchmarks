## Matrix Multiplication with 128-bit security
### Integer Domain

* HElib
  ```
  java -jar target/terminator-compiler-1.0.jar src/test/resources/mmult/matrixmult.t2 -HELIB -config src/test/resources/mmult/configs/helib-bfv-int-128.config
  ```

* Lattigo
  ```
  java -jar target/terminator-compiler-1.0.jar src/test/resources/mmult/matrixmult.t2 -LATTIGO
  ```

* PALISADE


* SEAL


* TFHE
  ```
  java -jar target/terminator-compiler-1.0.jar src/test/resources/mmult/matrixmult.t2 -TFHE
  ```
