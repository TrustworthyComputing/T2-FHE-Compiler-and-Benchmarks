<h1 align="center">Terminator Suite 2: Homomorphic Day <a href="https://github.com/TrustworthyComputing/Zilch/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg"></a> </h1>

<p align="center">
    <img src="./logos/t2-logo.png" height="20%" width="20%">
</p>
<h3 align="center">Data-Oblivious Benchmarks for Encrypted Data Computation</h3>


## Overview
The [TERMinator suite repository](https://github.com/momalab/TERMinatorSuite) offers benchmarks specifically tailored to encrypted computers, to enable comparisons across different architectures.
The original Terminator suite targeted partially homomorphic architectures.
However, since its release, fully homomorphic encryption (FHE) has become increasingly popular and more viable.
To account for this, Terminator 2 targets FHE architectures by modifying the original benchmarks as well as adding new additions that represent realistic use cases.


#### How to cite this work
The journal article describing the Terminator suite can be accessed [here](https://ieeexplore.ieee.org/document/8307166), while the authors' version is available [here](https://jimouris.github.io/publications/mouris2018terminator.pdf).
You can cite this article as follows:

```
D. Mouris, N. G. Tsoutsos and M. Maniatakos,
"TERMinator Suite: Benchmarking Privacy-Preserving Architectures."
IEEE Computer Architecture Letters, Volume: 17, Issue: 2, July-December 2018.
```

## Supported Homomorphic Encryption Libraries
* [Lattigo](https://github.com/ldsec/lattigo) v2.4.0 [55f9a02](https://github.com/ldsec/lattigo/commit/55f9a0247e2092a53be7630d6b2ca79021700a62)
* [HElib](https://github.com/homenc/HElib) v2.2.1 [f0e3e01](https://github.com/homenc/HElib/commit/f0e3e010009c592cd411ba96baa8376eb485247a)
* [PALISADE](https://gitlab.com/palisade/palisade-release/) v1.11.6 [08601274](https://gitlab.com/palisade/palisade-release/-/commit/0860127401ab794591f931fa2c61426c7b56ee2d)
* [Microsoft SEAL](https://github.com/microsoft/SEAL) v3.7.2 [7923472](https://github.com/microsoft/SEAL/commit/79234726053c45eede688400aa219fdec0810bd8)
* [TFHE](https://github.com/tfhe/tfhe) v1.0.1 [6297bc7](https://github.com/tfhe/tfhe/commit/6297bc72d9294e6e635738deb2e8dc7e4ff8bc61)


## Build and Run Instructions
### Dependencies
```powershell
apt install openjdk-8-jdk maven javacc
```

### Compile
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
