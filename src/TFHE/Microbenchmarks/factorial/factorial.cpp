#include <iostream>
#include <fstream>

#include <tfhe/tfhe.h>
#include <tfhe/tfhe_io.h>
#include <tfhe/tfhe_generic_streams.h>

#include "../../functional_units/functional_units.hpp"

using namespace std;

bool is_pow_of_2(int n) {
  int count = 0;
  for (int i = 0; i < 32; i++){
    count += (n >> i & 1);
  }
  return count == 1 && n > 0;
}

// Compute and return the Nth factorial.
vector<LweSample*> fact(vector<LweSample*>& prev_fact_, vector<LweSample*>& start_num_,
  const uint32_t N, const uint32_t word_sz,
  const TFheGateBootstrappingCloudKeySet* bk) {
  // Initialize result to Enc(0).
  vector<LweSample*> result_(1);
  result_[0] = new_gate_bootstrapping_ciphertext_array(word_sz, bk->params);
  for (int i = 0; i < word_sz; i++) {
    bootsCOPY(&result_[0][i], &prev_fact_[0][i], bk);
  }
  for (int i = 0; i < N; i++) {
    mult(result_, result_, start_num_, word_sz, bk);
    inc_inplace(start_num_, word_sz, bk);
  }
  return result_;
}

int main(int argc, char** argv) {
  // Argument sanity checks.
  std::ifstream cloud_key, ctxt_file;
  int word_sz = 0, N = 0;
  if (argc < 5) {
    std::cerr << "Usage: " << argv[0] <<
      " cloud_key ctxt_filename wordsize max_index" << std::endl <<
      "\tcloud_key: Path to the secret key" <<  std::endl <<
      "\tctxt_filename: Path to the ciphertext file" << std::endl <<
      "\twordsize: Number of bits per encrypted int" << std::endl <<
      "\tN: Number of iterations" << std::endl;
    return EXIT_FAILURE;
  } else {
    // Check if secret key file exists.
    cloud_key.open(argv[1]);
    if (!cloud_key) {
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
    // Check max index.
    N = atoi(argv[4]);
    if (N < 0 || N > 1000) {
      std::cerr << "Maximum iterations should be in [0, 1000]"<< std::endl;
      return EXIT_FAILURE;
    }
  }
  TFheGateBootstrappingCloudKeySet* bk =
    new_tfheGateBootstrappingCloudKeySet_fromStream(cloud_key);
  cloud_key.close();

  // If necessary, the params are inside the key.
  const TFheGateBootstrappingParameterSet* params = bk->params;

  // Read the ciphertext objects.
  uint32_t num_ctxts = 1;
  ctxt_file >> num_ctxts;
  vector<vector<LweSample*>> user_data(num_ctxts);
  for (int i = 0; i < num_ctxts; i++) {
    vector<LweSample*> tmp_vec(1);
    tmp_vec[0] = new_gate_bootstrapping_ciphertext_array(word_sz, params);
    user_data[i] = tmp_vec;
    for (int j = 0; j < word_sz; j++) {
      import_gate_bootstrapping_ciphertext_fromStream(ctxt_file, &user_data[i][0][j], params);
    }
  }
  ctxt_file.close();

  TIC(auto t1);
  vector<LweSample*> enc_result = fact(user_data[0], user_data[1], N, word_sz, bk);
  auto enc_time_ms = TOC_US(t1);
  std::cout << "Encrypted execution time " << enc_time_ms << " us" << std::endl;

  // Output result(s) to file.
  std::ofstream ctxt_out("output.ctxt");
  // The first line of ptxt_file contains the number of lines.
  ctxt_out << 1;
  for (int j = 0; j < word_sz; j++) {
    export_lweSample_toStream(ctxt_out, &enc_result[0][j], params->in_out_params);
  }
  delete_gate_bootstrapping_ciphertext_array(word_sz, enc_result[0]);
  ctxt_out.close();

  // Clean up all pointers.
  for (int i = 0; i < num_ctxts; i++) {
    delete_gate_bootstrapping_ciphertext_array(word_sz, user_data[i][0]);
  }
  delete_gate_bootstrapping_cloud_keyset(bk);
  return EXIT_SUCCESS;
}
