#include "../../functional_units/functional_units.hpp"

using namespace std;
using namespace lbcrypto;

int main(int argc, char** argv) {
  if (argc < 5) {
    cerr << "Usage: " << argv[0] << " n iter plaintext_modulus scheme" <<
      endl << "\tn: plaintext number to calculate factorial" <<
      endl << "\titer: number of iterations" <<
      endl << "\tplaintext_modulus: range of plaintext values" <<
      endl << "\tscheme: bfv, bgv" << endl;
    return EXIT_FAILURE;
  }
  size_t n = atoi(argv[1]);
  size_t iter = atoi(argv[2]);
  size_t plaintext_modulus = atoi(argv[3]); // 786433
  scheme_t scheme = NONE;
  if (strcmp(argv[4], "bfv") == 0) {
    scheme = BFV;
  } else if (strcmp(argv[4], "bgv") == 0) {
    scheme = BGV;
  } else {
    cerr << "Choose either bgv or bfv" << endl;
    return EXIT_FAILURE;
  }

  uint32_t depth = iter;
  double sigma = 3.2;
  SecurityLevel securityLevel = HEStd_128_classic;

  cout << "plaintext_modulus " << plaintext_modulus << endl;

  // Instantiate the crypto context.
  CryptoContext<DCRTPoly> cc;
  if (scheme == BFV) {
    cc = CryptoContextFactory<DCRTPoly>::genCryptoContextBFVrns(
          plaintext_modulus, securityLevel, sigma, 0, depth, 0, OPTIMIZED);
  } else { // BGV
    cc = CryptoContextFactory<DCRTPoly>::genCryptoContextBGVrns(
          // 1, 65537);
          depth, plaintext_modulus, securityLevel, sigma, depth, OPTIMIZED, BV);
  }
  // Enable features that you wish to use
  cc->Enable(ENCRYPTION);
  cc->Enable(SHE);
  if (scheme == BGV) {
    cc->Enable(LEVELEDSHE);
  }

  // Client: Key generation.
  auto keyPair = cc->KeyGen();
  // Generate the relinearization key.
  cc->EvalMultKeyGen(keyPair.secretKey);

  // Client: Calculate Fibonacci(n - 1).
  int64_t fact = 1;
  for (size_t i = 2; i < n; ++i) {
    fact *= i;
  }

  // Client: Encryption.
  vector<int64_t> tmpvec = { fact };
  auto tmp = cc->MakePackedPlaintext(tmpvec);
  auto fact_ = cc->Encrypt(keyPair.publicKey, tmp);
  tmpvec[0] = n;
  tmp = cc->MakePackedPlaintext(tmpvec);
  auto n_ = cc->Encrypt(keyPair.publicKey, tmp);
  tmpvec[0] = 1;
  tmp = cc->MakePackedPlaintext(tmpvec);
  auto one_ = cc->Encrypt(keyPair.publicKey, tmp);

  // Server: Run Fibonacci for iter iterations.
  Ciphertext<DCRTPoly> fib_;
  TIC(auto t1);
  for (size_t i = 0; i < iter; ++i) {
    fact_ = cc->EvalMultAndRelinearize(fact_, n_);  // fact_ *= n_
    n_ = cc->EvalAdd(n_, one_);       // n_ += Enc(1)
  }
  auto enc_time_ms = TOC_US(t1);
  cout << "Encrypted execution time " << enc_time_ms << " us" << endl;

  // Client: Decrypt the result.
  Plaintext result;
  cc->Decrypt(keyPair.secretKey, fact_, &result);
  cout << "Factorial(" << iter + n - 1 << ") = " << result << endl;

  return EXIT_SUCCESS;
}
