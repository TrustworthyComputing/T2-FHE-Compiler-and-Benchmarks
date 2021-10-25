#include <iostream>
#include <cassert>

#include "../../helper.hpp"

using namespace seal;
using namespace std;

#define word_sz 64  // For Simon 128/128, the word size is 64.
#define rounds 68 
#define plaintext_modulus 2

int main(int argc, char** argv) {
  // Argument sanity checks.
  std::ifstream ptxt_file, ctxt_file, key_file;
  if (argc < 5) {
    std::cerr << "Usage: " << argv[0] <<
      " ptxt_filename ctxt_filename rkey_filename" << std::endl <<
      "\tptxt_filename: Path to the expected ptxt (to verify)" <<  std::endl <<
      "\tctxt_filename: Path to the Simon ciphertext file" << std::endl <<
      "\trkey_filename: Path to the Simon round key file" << std::endl <<
      "\tpoly_modulus_degree: ciphertext degree" << std::endl;
    return EXIT_FAILURE;
  } else {
    // Check if ptxt file exists.
    ptxt_file.open(argv[1]);
    if (!ptxt_file) {
      std::cerr << "file " << argv[1] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
    // Check if ctxt file exists.
    ctxt_file.open(argv[2]);
    if (!ctxt_file) {
      std::cerr << "file " << argv[2] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
    // Check if rkey file exists.
    key_file.open(argv[3]);
    if (!key_file) {
      std::cerr << "file " << argv[3] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
  }
  size_t poly_modulus_degree = atoi(argv[4]);
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

  // Client: Encrypt Simon ciphertext and round keys.
  vector<vector<Ciphertext>> ctxt_(2);
  uint64_t curr_ctxt = 0;
  ctxt_file >> curr_ctxt;
  ctxt_[0] = encrypt_num_to_binary_array(encryptor, curr_ctxt, word_sz);
  ctxt_file >> curr_ctxt;
  ctxt_[1] = encrypt_num_to_binary_array(encryptor, curr_ctxt, word_sz);

  vector<vector<Ciphertext>> rkeys_(rounds);
  for (int i = 0; i < rounds; i++) {
    uint64_t curr_input = 0;
    key_file >> curr_input;
    rkeys_[i] = encrypt_num_to_binary_array(encryptor, curr_input, word_sz);
  }
  key_file.close();

  // Server: Run Simon decryption algorithm.
  TIC(auto t1);
  vector<vector<Ciphertext>> temp_(4);
  for (int i = rounds-1; i >= 0; i--) {
    cout << "Starting round " << i << endl;
    for (int j = 0; j < 4; j++) {
      temp_[j] = ctxt_[1];
    }
    // Generate rotations.
    rotate(temp_[0].begin(), temp_[0].begin() + 1, temp_[0].end());  // ROL-1
    rotate(temp_[1].begin(), temp_[1].begin() + 8, temp_[1].end());  // ROL-8
    rotate(temp_[2].begin(), temp_[2].begin() + 2, temp_[2].end());  // ROL-2

    // Compute AND and XOR operations.
    for (int j = 0; j < word_sz; j++) {
      evaluator.multiply_inplace(temp_[0][j], temp_[1][j]);   // ROL-1 & ROL-8
      evaluator.relinearize_inplace(temp_[0][j], relin_keys);
      evaluator.add_inplace(temp_[0][j], ctxt_[0][j]);  // (ROL-1 & ROL-8) ^ Ct[0]
      evaluator.add_inplace(temp_[0][j], temp_[2][j]);  // (ROL-1 & ROL-8) ^ Ct[0] ^ ROL-2
      evaluator.add(temp_[0][j], rkeys_[i][j], ctxt_[1][j]); // (ROL-1 & ROL-8) ^ Ct[0] ^ ROL-2 ^ RKEY
    }
    cout << "Noise budget in ctxt_1: " << decryptor.invariant_noise_budget(ctxt_[1][0]) << " bits" << endl;

    // Do Feistel Swap
    ctxt_[0] = temp_[3];

    vector<uint64_t> ptxt(2);
    ptxt[0] = decrypt_binary_array(decryptor, ctxt_[0]);
    ptxt[1] = decrypt_binary_array(decryptor, ctxt_[1]);
    cout << "ptxt[0]: " << ptxt[0] << endl;
    cout << "ptxt[1]: " << ptxt[1] << endl;

  }
  auto enc_time_ms = TOC_US(t1);
  cout << "Encrypted execution time " << enc_time_ms << " us" << endl;

  // Client: Decrypt and verify. 
  vector<uint64_t> ptxt(2), expected_ptxt(2);
  ctxt_file >> expected_ptxt[0];
  ctxt_file >> expected_ptxt[1];

  ptxt[0] = decrypt_binary_array(decryptor, ctxt_[0]);
  ptxt[1] = decrypt_binary_array(decryptor, ctxt_[1]);
  cout << "ptxt[0]: " << ptxt[0] << endl;
  cout << "ptxt[1]: " << ptxt[1] << endl;

  assert(ptxt[0] == expected_ptxt[0]);
  assert(ptxt[1] == expected_ptxt[1]);

  return EXIT_SUCCESS;
}
