### Fibonacci numbers
This algorithm receives an input `n` and return the `n`-th number in the Fibonacci sequence.

Formula:
```
F[1] = 1
F[2] = 1
F[n] = F[n-1] + F[n-2]
```
Output:
```
0 1 1 2 3 5 8 13 21 34 55 89 144 233 377 610 987 1587 ...
```

##### Implementation:
The algorithm computes the Fibonacci sequence up to a given maximum number (`max_num`), by iterating over all numbers in range `[0, max_num]`. This protects the input `n` against timing side-channels, since the execution time of Fibonacci is linear to that input. For that matter, the privacy-preserving version computes all Fibonacci numbers up to `max_num` and return only the result corresponding to input `n`.

