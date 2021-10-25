#include <iostream>
#include <fstream>
#include <tfhe/tfhe.h>
#include <tfhe/tfhe_io.h>
#include <tfhe/tfhe_generic_streams.h>

#include "../../helper.hpp"

#define word_sz 64  // For Simon 128/128, the word size is 64.
#define rounds 68 

void simon_128_128(LweSample** ctxt_, LweSample** rkeys_,
                   const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* tmp_[4];
  for (int i = 0; i < 4; i++) {
    tmp_[i] = new_gate_bootstrapping_ciphertext_array(word_sz, bk->params);
  }  
  // Start Simon decryption.
  for (int i = rounds-1; i >= 0; i--) {
    // Copy left ctxt block into temp vars.
    for (int j = 0; j < word_sz; j++) {
      bootsCOPY(&tmp_[0][j], &ctxt_[1][j], bk);  // ROL-1
      bootsCOPY(&tmp_[1][j], &ctxt_[1][j], bk);  // ROL-8
      bootsCOPY(&tmp_[2][j], &ctxt_[1][j], bk);  // ROL-2
      bootsCOPY(&tmp_[3][j], &ctxt_[1][j], bk);  // tmp
    }
    // Generate left rotations of left ctxt block.
    rotate_inplace(tmp_[0], LEFT, 1, word_sz, bk);
    rotate_inplace(tmp_[1], LEFT, 8, word_sz, bk);
    rotate_inplace(tmp_[2], LEFT, 2, word_sz, bk);
    // Compute AND and XOR operations.
    for (int j = 0; j < word_sz; j++) {
      bootsAND(&tmp_[0][j], &tmp_[0][j], &tmp_[1][j], bk);
      bootsXOR(&tmp_[0][j], &tmp_[0][j], &ctxt_[0][j], bk);
      bootsXOR(&tmp_[0][j], &tmp_[0][j], &tmp_[2][j], bk);
      bootsXOR(&ctxt_[1][j], &tmp_[0][j], &rkeys_[i][j], bk);
    }
    // Do Feistel swap.
    for (int j = 0; j < word_sz; j++) {
      bootsCOPY(&ctxt_[0][j], &tmp_[3][j], bk);
    }
  }
  for (int i = 0; i < 4; i++) {
    delete_gate_bootstrapping_ciphertext_array(word_sz, tmp_[i]);
  }
}

int main(int argc, char** argv) {
  // Argument sanity checks.
  std::ifstream cloud_key, ctxt_file, key_file;
  if (argc < 4) {
    std::cerr << "Usage: " << argv[0] <<
      " cloud_key db_filename query_filename wordsize max_index" << std::endl <<
      "\tcloud_key: Path to the secret key" <<  std::endl <<
      "\tctxt_filename: Path to the encrypted Simon ciphertext file" << std::endl <<
      "\tkey_filename: Path to the encrypted Simon key file" << std::endl;
    return EXIT_FAILURE;
  } else {
    // Check if cloud key file exists.
    cloud_key.open(argv[1]);
    if (!cloud_key) {
      std::cerr << "file " << argv[1] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
    // Check if ctxt file exists.
    ctxt_file.open(argv[2]);
    if (!ctxt_file) {
      std::cerr << "file " << argv[2] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
    // Check if encrypted key file exists.
    key_file.open(argv[3]);
    if (!key_file) {
      std::cerr << "file " << argv[3] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
  }

  TFheGateBootstrappingCloudKeySet* bk =
    new_tfheGateBootstrappingCloudKeySet_fromStream(cloud_key);
  cloud_key.close();

  // If necessary, the params are inside the key.
  const TFheGateBootstrappingParameterSet* params = bk->params;

  // Read HE(Simon(ptxt)) in two blocks.
  uint32_t num_ctxts = 2;
  ctxt_file >> num_ctxts;
  assert(("Ctxt should be 2 blocks of 64 bits", num_ctxts == 2));
  LweSample* simon_ctxt_[num_ctxts];
  for (int i = 0; i < num_ctxts; i++) {
    simon_ctxt_[i] = new_gate_bootstrapping_ciphertext_array(word_sz, params);
    for (int j = 0; j < word_sz; j++) {
      import_gate_bootstrapping_ciphertext_fromStream(ctxt_file,
                                                      &simon_ctxt_[i][j], params);
    }
  }
  ctxt_file.close();

  // Read round keys.
  uint32_t num_rkeys = 68;
  key_file >> num_rkeys;
  assert(("There should be 68 round keys (64 bits each)", num_rkeys == rounds));
  LweSample* rkeys_[rounds];
  for (int i = 0; i < rounds; i++) {
    rkeys_[i] = new_gate_bootstrapping_ciphertext_array(word_sz, params);
    for (int j = 0; j < word_sz; j++) {
      import_gate_bootstrapping_ciphertext_fromStream(key_file, 
        &rkeys_[i][j], params);
    }
  }
  key_file.close();

  simon_128_128(simon_ctxt_, rkeys_, bk);

  // Output result(s) to file.
  std::ofstream ctxt_out("output.ctxt");
  // The first line of ptxt_file contains the number of lines.
  ctxt_out << 2;
  for (int i = 0; i < 2; i++) {
    for (int j = 0; j < word_sz; j++) {
      export_lweSample_toStream(ctxt_out, &simon_ctxt_[i][j], params->in_out_params);
    }
    delete_gate_bootstrapping_ciphertext_array(word_sz, simon_ctxt_[i]);
  }
  ctxt_out.close();

  // Clean up all pointers.
  for (int i = 0; i < rounds; i++) {
    delete_gate_bootstrapping_ciphertext_array(word_sz, rkeys_[i]);
  }
  delete_gate_bootstrapping_cloud_keyset(bk);
  return EXIT_SUCCESS;
}
