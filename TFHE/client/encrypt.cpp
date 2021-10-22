#include <iostream>
#include <fstream>
#include <sstream>

#include <tfhe/tfhe.h>
#include <tfhe/tfhe_io.h>
#include <tfhe/tfhe_generic_streams.h>

#include "../helper.hpp"

int main(int argc, char** argv) {
  // Argument sanity checks.
  std::ifstream secret_key, ptxt_file;
  int word_sz = 0;
  int db = 0;
  std::string output_filename;
  if (argc < 6) {
    std::cerr << "Usage: " << argv[0] << " secret_key ptxt_filename wordsize db output_filename" <<
      std::endl << "\tsecret_key: Path to the secret key" <<
      std::endl << "\tptxt_filename: Path to the plaintext file" <<
      std::endl << "\twordsize: Number of bits per encrypted int" <<
      std::endl << "\tdb: 1 for KV format, 0 otherwise (no db)" <<
      std::endl << "\toutput_filename: the name of the output ciphertext file" << std::endl;
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
    // Check db.
    db = atoi(argv[4]);
    if (db < 0 || db > 1) {
      std::cerr << "db should be 0 or 1" << std::endl;
      return EXIT_FAILURE;
    }
    output_filename = argv[5];
  }
  TFheGateBootstrappingSecretKeySet* key =
    new_tfheGateBootstrappingSecretKeySet_fromStream(secret_key);
  secret_key.close();

  // If necessary, the params are inside the key.
  const TFheGateBootstrappingParameterSet* params = key->params;

  // Read and encrypt plaintext inputs.
  std::ofstream ctxt_out(output_filename);
  uint32_t input_len = 0;
  // The first line of ptxt_file contains the number of lines.
  ptxt_file >> input_len;
  if (db) {
    ctxt_out << (input_len*2);
    LweSample* ctxt_in[input_len*2];
    uint64_t keys[input_len];
    uint64_t vals[input_len];
    std::string input = "";
    for (int i = 0; i < input_len*2; i+=2) {
      ctxt_in[i] = new_gate_bootstrapping_ciphertext_array(word_sz, params);
      ctxt_in[i+1] = new_gate_bootstrapping_ciphertext_array(word_sz, params);
      ptxt_file >> input;
      size_t pos = input.find(':');
      std::string fst = input.substr(0, pos);
      std::string snd = input.substr(pos + 1, input.size() - 1);
      std::stringstream conv_fst(fst);
      std::stringstream conv_snd(snd);
      conv_fst >> keys[i/2];
      conv_snd >> vals[i/2];
      for (int j = 0; j < word_sz; j++) {
        bootsSymEncrypt(&ctxt_in[i][j], (keys[i/2] >> j) & 1, key);
        export_lweSample_toStream(ctxt_out, &ctxt_in[i][j], params->in_out_params);
      }
      for (int j = 0; j < word_sz; j++) {
        bootsSymEncrypt(&ctxt_in[i+1][j], (vals[i/2] >> j) & 1, key);
        export_lweSample_toStream(ctxt_out, &ctxt_in[i+1][j], params->in_out_params);
      }
    }
    for (int i = 0; i < input_len; i++) {
      delete_gate_bootstrapping_ciphertext_array(word_sz, ctxt_in[i]);
    }
  } else {
    ctxt_out << input_len;
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
    for (int i = 0; i < input_len; i++) {
      delete_gate_bootstrapping_ciphertext_array(word_sz, ctxt_in[i]);
    }
  }
  ptxt_file.close();

  // Clean up all pointers.
  delete_gate_bootstrapping_secret_keyset(key);
  return EXIT_SUCCESS;
}
