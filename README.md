<h1 align="center">Terminator Suite 2: Homomorphic Day <a href="https://github.com/TrustworthyComputing/Zilch/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg"></a> </h1>

<p align="center">
    <img src="./logos/t2-logo.png" height="20%" width="20%">
</p>
<h3 align="center">Data-Oblivious Benchmarks for Encrypted Data Computation</h3>


## Overview
T2 is an extensible compiler and benchmark suite enabling comparisons between
fully-homomorphic encryption (FHE) libraries. The T2 repository comprises the
T2 DSL and the T2 compiler from T2 DSL to state-of-the-art FHE back-ends.


T2 aims to offer a standardized benchmark suite for FHE that encompasses
realistic use-cases. Additionally, the T2 compiler offers a great starting
point to explore the different backends (e.g., HElib, Lattigo, PALISADE, SEAL,
and TFHE) as a single T2 program is effortlessly transpiled to all supported
FHE libraries.

The T2 compiler supports three distinct computational models: *Integer*, *Binary*,
and *Floating-Point* domains. Each domain utilizes different sets of functional
units, some of which are inherently supported by the backends, while others are
implemented on top of the FHE back-ends. This allows users to compare the
different domains and find the most efficient for their applications.

T2 is the spiritual successor to the [TERMinator suite
repository](https://github.com/momalab/TERMinatorSuite),
which includes benchmarks tailored to encrypted computation. The original
Terminator suite targets partially homomorphic architectures, however, since its
release, fully homomorphic encryption (FHE) has become increasingly popular and
more viable. To account for this, T2 targets FHE architectures by modifying the
original benchmarks as well as adding new additions.


### How to cite this work
The journal article describing the Terminator suite can be accessed [here](https://ieeexplore.ieee.org/document/8307166), while the authors' version is available [here](https://jimouris.github.io/publications/mouris2018terminator.pdf).
You can cite this article as follows:

```
D. Mouris, N. G. Tsoutsos and M. Maniatakos,
"TERMinator Suite: Benchmarking Privacy-Preserving Architectures."
IEEE Computer Architecture Letters, Volume: 17, Issue: 2, July-December 2018.
```

## Supported Homomorphic Encryption Libraries

<div style="background-color:#FFFF; color:#1A2067; border: solid #718096 4px; border-radius: 4px;">
<p>
  <img src="./logos/t2-compiler.png" align="right" height="40%" width="40%" padding=10em>
  <a href="https://github.com/tuneinsight/lattigo">Lattigo</a> v3.0.2 <a href="https://github.com/tuneinsight/lattigo/commit/27fee8bebbb5ee600d69086f0b5a8ff9a6c8e24e">27fee8b</a>
  <br>
  <a href="https://github.com/homenc/HElib">HElib</a> v2.2.1 <a href="https://github.com/homenc/HElib/commit/f0e3e010009c592cd411ba96baa8376eb485247a">f0e3e01</a>
  <br>
  <a href="https://gitlab.com/palisade/palisade-release/">PALISADE</a> v1.11.6 <a href="https://gitlab.com/palisade/palisade-release/-/commit/0860127401ab794591f931fa2c61426c7b56ee2d">08601274</a>
  <br>
  <a href="https://github.com/microsoft/SEAL">Microsoft SEAL</a> v4.0.0 <a href="https://github.com/microsoft/SEAL/commit/a0fc0b732f44fa5242593ab488c8b2b3076a5f76">a0fc0b7</a>
  <br>
  <a href="https://github.com/tfhe/tfhe">TFHE</a> v1.0.1 <a href="https://github.com/tfhe/tfhe/commit/6297bc72d9294e6e635738deb2e8dc7e4ff8bc61">6297bc7</a>
  <br>
</p>
</div>
<br>


## Build and Run Instructions
### Dependencies
*
  ```powershell
  apt install cmake make build-essential g++ clang autoconf javacc patchelf openjdk-8-jdk maven m4 tar lzip libfftw3-dev
  ```
* [Go-lang](https://go.dev/dl/)
* Follow and modify [clone_libs.sh](./.circleci/clone_libs.sh) and
  [build_libs.sh](./.circleci/build_libs.sh) scripts to install the FHE libraries in your custom destinations.

### Compile the T2 compiler
```powershell
mvn initialize package
```

To skip running the test when compiling the T2 compiler run:
```powershell
mvn package -Dmaven.test.skip
```

### Compile T2 programs
To compile a T2 program type:
```powershell
java -jar target/terminator-compiler-1.0.jar <path_to_t2_file> [--debug] <LIB> [--w word_size]
```
where `<LIB>` can be one of `HElib`, `Lattigo`, `SEAL`, `PALISADE`, and `TFHE`. For instance:
```powershell
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/arithmetic.t2 --seal
```
will use `SEAL` as the back-end over the integers, whereas:
```powershell
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/bin_test.t2 --lattigo --w 6
```
will use `Lattigo` as the back-end in the binary domain. The T2 compiler
automatically detects the appropriate scheme (i.e., `BFV/BGV` or `CKKS`) based
on the type of the encrypted variables that the T2 program uses (i.e.,
`EncInt` or `EncDouble`).
For example:
```powershell
java -jar target/terminator-compiler-1.0.jar src/test/resources/tests/ckks_test.t2 --seal
```
will use `SEAL` with the `CKKS` scheme.


<p align="center">
    <img src="./logos/twc.png" height="20%" width="20%">
</p>
<h4 align="center">Trustworthy Computing Group</h4>
