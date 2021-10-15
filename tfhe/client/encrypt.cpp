#include <iostream>
#include <fstream>

#include <tfhe/tfhe.h>
#include <tfhe/tfhe_io.h>
#include <tfhe/tfhe_generic_streams.h>

#include "../helper.hpp"

int main(int argc, char** argv) {
  // Argument sanity checks.
  std::ifstream secret_key, ptxt_file;
  int word_sz = 0;
  if (argc < 4) {
    std::cerr << "Usage: " << argv[0] << " secret_key ptxt_filename wordsize" <<
      std::endl << "\tsecret_key: Path to the secret key" << 
      std::endl << "\tptxt_filename: Path to the plaintext file" <<
      std::endl << "\twordsize: Number of bits per encrypted int" << std::endl;
    return EXIT_FAILURE;
  } else {
    // Check if secret key file exists.
    secret_key.open(argv[1]);
    if (!secret_key) {
      std::cerr << "file " << argv[1] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
    // Check if plaintext file exists.
    ptxt_file.open(argv[2]);
    if (!ptxt_file) {
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

  // Read and encrypt plaintext inputs.
  std::ofstream ctxt_out("input.ctxt");
  uint32_t input_len = 0;
  // The first line of ptxt_file contains the number of lines.
  ptxt_file >> input_len;
  ctxt_out << input_len;
  // ctxt_out << '\n';
  LweSample* ctxt_in[input_len];
  uint64_t curr_input = 0;
  for (int i = 0; i < input_len; i++) {
    ctxt_in[i] = new_gate_bootstrapping_ciphertext_array(word_sz, params);
    ptxt_file >> curr_input;
    for (int j = 0; j < word_sz; j++) {
      bootsSymEncrypt(&ctxt_in[i][j], (curr_input >> j) & 1, key);
      export_lweSample_toStream(ctxt_out, &ctxt_in[i][j], params->in_out_params);
    }
  }
  ptxt_file.close();

  // Clean up all pointers.
  for (int i = 0; i < input_len; i++) {
    delete_gate_bootstrapping_ciphertext_array(word_sz, ctxt_in[i]);
  }
  delete_gate_bootstrapping_secret_keyset(key);
  return EXIT_SUCCESS;
}
