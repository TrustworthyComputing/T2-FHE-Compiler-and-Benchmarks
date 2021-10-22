#include <stdio.h>
#include <string.h>
#include <iostream>
#include <fstream>

#include <vector>

#include "palisade.h"

using namespace std;
using namespace lbcrypto;

int main(int argc, char** argv) {
  if (argc < 4) {
    cerr << "Usage: " << argv[0] << " n iter poly_modulus_degree plaintext_modulus" <<
      endl << "\tn: plaintext number to calculate Fibonacci" <<
      endl << "\titer: number of iterations" <<
      endl << "\tplaintext_modulus: range of plaintext values" << endl;
    return EXIT_FAILURE;
  }
  size_t n = atoi(argv[1]);
  size_t iter = atoi(argv[2]);
  size_t plaintext_modulus = atoi(argv[3]); // 786433

  uint32_t multDepth = 3;
  double sigma = 3.2;
  SecurityLevel securityLevel = HEStd_128_classic;

  cout << "plaintext_modulus " << plaintext_modulus << endl;

  // Instantiate the crypto context.
  CryptoContext<DCRTPoly> cc =
  CryptoContextFactory<DCRTPoly>::genCryptoContextBFVrns(
    plaintext_modulus, securityLevel, sigma, 0, multDepth, 0, OPTIMIZED);

  // Enable features that you wish to use
  cc->Enable(ENCRYPTION);
  cc->Enable(SHE);

  // Client: Key generation.
  auto keyPair = cc->KeyGen();
  // Generate the relinearization key.
  cc->EvalMultKeyGen(keyPair.secretKey);

  // Client: Calculate Fibonacci(n - 1).
  int64_t f1 = 0, f2 = 1, fib = 1;
  for (size_t i = 2; i < n; ++i) {
    fib = f1 + f2;
    f1 = f2;
    f2 = fib;
  }

  // Client: Encryption.
  vector<int64_t> tmpvec = { f1 };
  auto tmp = cc->MakePackedPlaintext(tmpvec);
  auto f1_ = cc->Encrypt(keyPair.publicKey, tmp);
  tmpvec[0] = f2;
  tmp = cc->MakePackedPlaintext(tmpvec);
  auto f2_ = cc->Encrypt(keyPair.publicKey, tmp);
  tmpvec[0] = n;
  tmp = cc->MakePackedPlaintext(tmpvec);
  auto n_ = cc->Encrypt(keyPair.publicKey, tmp);

  // Server: Run Fibonacci for iter iterations.
  Ciphertext<DCRTPoly> fib_;
  TIC(auto t1);
  for (size_t i = 0; i < iter; ++i) {
    fib_ = cc->EvalAdd(f1_, f2_);  // fib_ = f1_ + f2_
    f1_ = f2_;
    f2_ = fib_;
  }
  auto enc_time_ms = TOC_US(t1);
  cout << "Encrypted execution time " << enc_time_ms << " us" << endl;

  // Client: Decrypt the result.
  Plaintext result;
  cc->Decrypt(keyPair.secretKey, fib_, &result);
  cout << "Fibonacci(" << iter + n - 1 << ") = " << result << endl;

  return EXIT_SUCCESS;
}
