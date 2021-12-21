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

vector<LweSample*> pir(vector<vector<LweSample*>>& db_, const vector<LweSample*>& query_, int word_sz,
               int num_entries, const TFheGateBootstrappingCloudKeySet* bk) {
  // Initialize output ctxt to 0 and declare temp var for equality.
  vector<LweSample*> result_(1);
  result_[0] = new_gate_bootstrapping_ciphertext_array(word_sz,
    bk->params);
  vector<LweSample*> control_(1);
  control_[0] = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  for (int i = 0; i < word_sz; i++) {
    bootsCONSTANT(&result_[0][i], 0, bk);
  }

  // Compute equality with each key and load value of match to output.
  for (int i = 0; i < num_entries; i++) {
    eq(control_, query_, db_[i*2], word_sz, bk);
    for (int j = 0; j < word_sz; j++) {
      bootsMUX(&result_[0][j], &control_[0][0], &db_[i*2+1][0][j], &result_[0][j], bk);
    }
  }
  delete_gate_bootstrapping_ciphertext_array(1, control_[0]);
  return result_;
}

int main(int argc, char** argv) {
  // Argument sanity checks.
  std::ifstream cloud_key, db_file, query_file;
  int word_sz = 0;
  if (argc < 5) {
    std::cerr << "Usage: " << argv[0] <<
      " cloud_key db_filename query_filename wordsize max_index" << std::endl <<
      "\tcloud_key: Path to the secret key" <<  std::endl <<
      "\tdb_filename: Path to the DB ciphertext file" << std::endl <<
      "\tquery_filename: Path to the query ciphertext file" << std::endl <<
      "\twordsize: Number of bits per encrypted int" << std::endl;
    return EXIT_FAILURE;
  } else {
    // Check if secret key file exists.
    cloud_key.open(argv[1]);
    if (!cloud_key) {
      std::cerr << "file " << argv[1] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
    // Check if DB file exists.
    db_file.open(argv[2]);
    if (!db_file) {
      std::cerr << "file " << argv[2] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
    // Check if query file exists.
    query_file.open(argv[3]);
    if (!query_file) {
      std::cerr << "file " << argv[3] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
    // Check wordsize.
    word_sz = atoi(argv[4]);
    if (word_sz < 8 || word_sz > 64 || !is_pow_of_2(word_sz)) {
      std::cerr << "wordsize should be a power of 2 in the range 2^3 - 2^6"
        << std::endl;
      return EXIT_FAILURE;
    }
  }

  TFheGateBootstrappingCloudKeySet* bk =
    new_tfheGateBootstrappingCloudKeySet_fromStream(cloud_key);
  cloud_key.close();

  // If necessary, the params are inside the key.
  const TFheGateBootstrappingParameterSet* params = bk->params;

  // Read the KV ciphertexts from the DB.
  uint32_t num_ctxts = 2;
  db_file >> num_ctxts;
  vector<vector<LweSample*>> user_data(num_ctxts);
  for (int i = 0; i < num_ctxts; i++) {
    vector<LweSample*> tmp_vec(1);
    tmp_vec[0] = new_gate_bootstrapping_ciphertext_array(word_sz, params);
    user_data[i] = tmp_vec;
    for (int j = 0; j < word_sz; j++) {
      import_gate_bootstrapping_ciphertext_fromStream(db_file,
                                                      &user_data[i][0][j], params);
    }
  }
  db_file.close();

  // Read encrypted query.
  uint32_t num_queries = 1;
  query_file >> num_queries;
  assert(("This benchmark only supports one query at a time",
    num_queries == 1));
  vector<LweSample*> query_(1);
  query_[0] = new_gate_bootstrapping_ciphertext_array(word_sz, params);
  for (int i = 0; i < word_sz; i++) {
    import_gate_bootstrapping_ciphertext_fromStream(query_file,
      &query_[0][i], params);
  }
  query_file.close();

  TIC(auto t1);
  vector<LweSample*> enc_result = pir(user_data, query_, word_sz, num_ctxts/2, bk);
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
