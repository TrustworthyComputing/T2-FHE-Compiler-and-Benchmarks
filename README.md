<h1 align="center">Terminator Suite 2: Homomorphic Days <a href="https://github.com/TrustworthyComputing/Zilch/blob/master/LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg"></a> </h1>
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

## List of Terminator Suite 2 Benchmarks

#### Synthetic Category
<!-- In this class we have the `NQueens` and `Tak` algorithms, which evaluate the universality of the underlying abstract machine using recursion. -->

<!-- 1. __[N-Queens](https://github.com/momalab/TERMinatorSuite/blob/master/Synthetic/nqueens)__ [(link)](http://www.kotesovec.cz/rivin_1994.pdf) -->

<!-- 1. __[Tak function](https://github.com/momalab/TERMinatorSuite/blob/master/Synthetic/tak_function)__ [(link)](http://www.users.miamioh.edu/ishiut/papers/tarai_ipl.pdf) -->

#### Encoder Benchmarks Category
<!-- This class comprises three real-life cryptographic and hash applications (namely `Speck`, `Simon` and `Jenkins`), which are demanding in terms of bitwise operations. -->

<!-- 1. __[Speck (cipher)](https://github.com/momalab/TERMinatorSuite/blob/master/EncoderBenchmarks/SpeckCipher)__ [(link)](https://eprint.iacr.org/2013/404.pdf) -->

<!-- 1. __[Simon (cipher)](https://github.com/momalab/TERMinatorSuite/blob/master/EncoderBenchmarks/SimonCipher)__ [(link)](https://eprint.iacr.org/2013/404.pdf) -->

<!-- 1. __[Jenkins one-at-a-time Hash Function](https://github.com/momalab/TERMinatorSuite/blob/master/EncoderBenchmarks/jenkinsHash)__ [(link)](http://www.burtleburtle.net/bob/hash/doobs.html) -->

#### Kernels Category
<!-- In this class we have the `Insertion Sort`, `Set Intersection`, `Deduplication (Union)`, `Matrix Multiplication`, `Primes (Sieve of Eratosthenes)`, and `Permutations`, which evaluate essential loops that combine memory swaps and arithmetic operations. These kernels also have significance in privacy-sensitive real-life applications: for example, set intersection is used to evaluate collision courses of military satellites without revealing actual paths, while permutations is an important part of DNA sequencing. -->

<!-- 1. __[Insertion-sort](https://github.com/momalab/TERMinatorSuite/blob/master/Kernels/insertionSort)__ [(link)](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.45.8017&rep=rep1&type=pdf) -->

<!-- 1. __[Private Set Intersection](https://github.com/momalab/privacy_benchmarks/tree/master/Kernels/PSI)__ [(link)](https://www.cs.virginia.edu/~evans/pubs/ndss2012/psi.pdf) -->

<!-- 1. __[Data Deduplication Algorithm](https://github.com/momalab/TERMinatorSuite/blob/master/Kernels/deduplication)__ [(link)](https://dl.acm.org/citation.cfm?id=1456471) -->

<!-- 1. __[Permutations](https://github.com/momalab/TERMinatorSuite/blob/master/Kernels/permutations)__ [(link)](http://mathworld.wolfram.com/Permutation.html) -->

<!-- 1. __[Sieve of Eratosthenes](https://github.com/momalab/TERMinatorSuite/blob/master/Kernels/sieveOfEratosthenes)__ [(link)](http://mathworld.wolfram.com/SieveofEratosthenes.html) -->

<!-- 1. __[Matrix Multiplication](https://github.com/momalab/TERMinatorSuite/blob/master/Kernels/matrixMultiplication)__ [(link)](http://mathworld.wolfram.com/MatrixMultiplication.html) -->

#### Microbenchmarks Category
<!-- In this class we have the `Factorial`, `Fibonacci` and `Private Information Retrieval` algorithms, which evaluate a single critical homomorphic operation of addition and multiplication in the underlying abstract machine. -->

<!-- 1. __[Private Information Retrieval](https://github.com/momalab/TERMinatorSuite/blob/master/Microbenchmarks/PIR)__ [(link)](https://crysp.uwaterloo.ca/courses/pet/F09/cache/www.dbis.informatik.hu-berlin.de/fileadmin/research/papers/conferences/2001-gi_ocg-asonov.pdf) -->

<!-- 1. __[Factorial](https://github.com/momalab/TERMinatorSuite/blob/master/Microbenchmarks/factorial)__ [(link)](http://mathworld.wolfram.com/Factorial.html) -->

<!-- 1. __[Fibonacci](https://github.com/momalab/TERMinatorSuite/blob/master/Microbenchmarks/fibonacci)__ [(link)](http://mathworld.wolfram.com/FibonacciNumber.html) -->

#### Other Algorithms
<!-- 1. __[Number Occurrences](https://github.com/momalab/TERMinatorSuite/blob/master/OtherAlgorithms/numOccurrences)__ -->



<p align="center">
    <img src="./logos/twc.png" height="20%" width="20%">
</p>
<h4 align="center">Trustworthy Computing Group</h4>
