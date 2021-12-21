#include <iostream>
#include <fstream>

#include <tfhe/tfhe.h>
#include <tfhe/tfhe_io.h>
#include <tfhe/tfhe_generic_streams.h>

bool is_pow_of_2(int n) {
  int count = 0;
  for (int i = 0; i < 32; i++){
    count += (n >> i & 1);
  }
  return count == 1 && n > 0;
}

int main(int argc, char** argv) {
  // Argument sanity checks.
  std::ifstream secret_key, ctxt_file;
  int word_sz = 0;
  if (argc < 4) {
    std::cerr << "Usage: " << argv[0] << " secret_key ctxt_filename wordsize" <<
      std::endl << "\tsecret_key: Path to the secret key" <<
      std::endl << "\tctxt_filename: Path to the ciphertext file" <<
      std::endl << "\twordsize: Number of bits per encrypted int" << std::endl;
    return EXIT_FAILURE;
  } else {
    // Check if secret key file exists.
    secret_key.open(argv[1]);
    if (!secret_key) {
      std::cerr << "file " << argv[1] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
    // Check if ciphertext file exists.
    ctxt_file.open(argv[2]);
    if (!ctxt_file) {
      std::cerr << "file " << argv[2] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
    // Check wordsize.
    word_sz = atoi(argv[3]);
    if (word_sz < 8 || word_sz > 64 || !is_pow_of_2(word_sz)) {
      std::cerr << "wordsize should be a power of 2 in the range 2^3 - 2^6"
        << std::endl;
      return EXIT_FAILURE;
    }
  }
  TFheGateBootstrappingSecretKeySet* key =
    new_tfheGateBootstrappingSecretKeySet_fromStream(secret_key);
  secret_key.close();

  // If necessary, the params are inside the key.
  const TFheGateBootstrappingParameterSet* params = key->params;

  // Read and decrypt ciphertexts.
  uint32_t num_ctxts = 0;
  ctxt_file >> num_ctxts;
  LweSample* answer[num_ctxts];
  uint64_t int_answer[num_ctxts] = { 0 };
  for (int i = 0; i < num_ctxts; i++) {
    answer[i] = new_gate_bootstrapping_ciphertext_array(word_sz, params);
    for (int j = 0; j < word_sz; j++) {
      import_gate_bootstrapping_ciphertext_fromStream(ctxt_file,
                                                      &answer[i][j], params);
      uint64_t ai = bootsSymDecrypt(&answer[i][j], key) > 0;
      int_answer[i] |= (uint64_t)(ai << j);
    }
    std::cout << "Answer " << i << ": " << int_answer[i] << std::endl;
  }
  ctxt_file.close();

  // Clean up all pointers.
  for (int i = 0; i < num_ctxts; i++) {
    delete_gate_bootstrapping_ciphertext_array(word_sz, answer[i]);
  }
  delete_gate_bootstrapping_secret_keyset(key);
  return EXIT_SUCCESS;
}
