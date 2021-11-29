#include <iostream>
#include <fstream>

#include "../../helper.hpp"

using namespace seal;
using namespace std;

int main(int argc, char** argv) {
  if (argc < 4) {
    std::cerr << "Usage: " << argv[0] << " n iter poly_modulus_degree plaintext_modulus" <<
      std::endl << "\tn: plaintext number to calculate factorial" <<
      std::endl << "\titer: number of iterations" <<
      std::endl << "\tpoly_modulus_degree: ciphertext degree" <<
      std::endl << "\tplaintext_modulus: range of plaintext values" << std::endl;
    return EXIT_FAILURE;
  }
  size_t n = atoi(argv[1]);
  size_t iter = atoi(argv[2]);
  size_t poly_modulus_degree = atoi(argv[3]);
  size_t plaintext_modulus = atoi(argv[4]);
  EncryptionParameters parms(scheme_type::bfv);
  parms.set_poly_modulus_degree(poly_modulus_degree);
  parms.set_coeff_modulus(CoeffModulus::BFVDefault(poly_modulus_degree));
  parms.set_plain_modulus(plaintext_modulus);
  SEALContext context(parms);
  print_parameters(context);
  KeyGenerator keygen(context);
  SecretKey secret_key = keygen.secret_key();
  PublicKey public_key;
  RelinKeys relin_keys;
  keygen.create_public_key(public_key);
  keygen.create_relin_keys(relin_keys);
  Encryptor encryptor(context, public_key);
  Evaluator evaluator(context);
  Decryptor decryptor(context, secret_key);

  // Client: Calculate factorial(n - 1).
  uint64_t fact = 1;
  for (int i = 2; i < n; ++i) {
    fact *= i;
  }
  Plaintext one("1"), tmp(uint64_to_hex_string(fact));
  Ciphertext fact_, n_;
  encryptor.encrypt(tmp, fact_);
  tmp = uint64_to_hex_string(n);
  encryptor.encrypt(tmp, n_);

  // Server: Run factorial for iter iterations.
  TIC(auto t1);
  for (int i = 0; i < iter; ++i) {
    evaluator.multiply_inplace(fact_, n_);  // fact_ *= n_
    evaluator.relinearize_inplace(fact_, relin_keys);
    evaluator.add_plain_inplace(n_, one);   // n_ += Enc(1)
  }
  auto enc_time_ms = TOC_US(t1);
  cout << "Encrypted execution time " << enc_time_ms << " us" << endl;

  // Client: Decrypt.
  Plaintext result;
  decryptor.decrypt(fact_, result);
  cout << "Factorial(" << iter + n - 1 << ") = " << result << endl;

  return EXIT_SUCCESS;
}
