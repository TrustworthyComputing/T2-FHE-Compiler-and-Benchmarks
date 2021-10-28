#include <iostream>
#include <cassert>

#include "../../helper.hpp"

using namespace seal;
using namespace std;

#define ptxt_mod_size 60  // number of bits
// #define scale_factor 100.0*48.0
#define scale_factor 48.0

int main(int argc, char** argv) {
  // Argument sanity checks.
  std::ifstream data_file, weights_file;
  if (argc < 4) {
    std::cerr << "Usage: " << argv[0] <<
      " data_filename weights_filename poly_modulus_degree" << std::endl <<
      "\tdata_filename: Path to the data to be classified" <<  std::endl <<
      "\tweights_filename: Path to the pre-trained LR weights" << std::endl <<
      "\tpoly_modulus_degree: ciphertext degree" << std::endl;
    return EXIT_FAILURE;
  } else {
    // Check if data file exists.
    data_file.open(argv[1]);
    if (!data_file) {
      std::cerr << "file " << argv[1] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
    // Check if ctxt file exists.
    weights_file.open(argv[2]);
    if (!weights_file) {
      std::cerr << "file " << argv[2] << " does not exist" << std::endl;
      return EXIT_FAILURE;
    }
  }
  size_t poly_modulus_degree = atoi(argv[3]);
  EncryptionParameters parms(scheme_type::bfv);
  parms.set_poly_modulus_degree(poly_modulus_degree);
  parms.set_coeff_modulus(CoeffModulus::BFVDefault(poly_modulus_degree));
  parms.set_plain_modulus(PlainModulus::Batching(poly_modulus_degree,
                                                 ptxt_mod_size));
  SEALContext context(parms);
  auto qualifiers = context.first_context_data()->qualifiers();
  assert(("Batching enabled", qualifiers.using_batching == 1));
  print_parameters(context);
  KeyGenerator keygen(context);
  SecretKey secret_key = keygen.secret_key();
  PublicKey public_key;
  RelinKeys relin_keys;
  GaloisKeys galois_keys;
  keygen.create_galois_keys(galois_keys);
  keygen.create_public_key(public_key);
  keygen.create_relin_keys(relin_keys);
  Encryptor encryptor(context, public_key);
  Evaluator evaluator(context);
  Decryptor decryptor(context, secret_key);
  BatchEncoder batch_encoder(context);

  // Client: Read plaintext weights and encrypt classification inputs.
  size_t num_attributes = 2;
  size_t num_inferences = 1;

  // Read plaintext weights.
  weights_file >> num_attributes;
  vector<Plaintext> weights(num_attributes);
  double curr_input = 0.0;
  for (int i = 0; i < num_attributes; i++) {
    weights_file >> curr_input;
    weights[i] =
      uint64_to_hex_string(static_cast<uint64_t>(curr_input * scale_factor));
  }
  weights_file.close();

  // Read and encrypt data.
  data_file >> num_inferences;
  data_file >> num_attributes;
  size_t slots = poly_modulus_degree / 2;
  // Batching: Multiple classifications at once would yield higher throughput.
  vector<Ciphertext> ctxt_(num_attributes);
  assert(("Number of inferences must not be greater than number of slots",
          slots > num_inferences));
  vector<vector<uint64_t>> lr_inputs(num_attributes,
                                     vector<uint64_t>(num_inferences, 0));
  for (size_t i = 0; i < num_inferences; ++i) {
    for (size_t j = 0; j < num_attributes; ++j) {
      data_file >> curr_input;
      lr_inputs[j][i] = static_cast<uint64_t>(curr_input * scale_factor);
    }
  }
  for (size_t i = 0; i < num_attributes; i++) {
    ctxt_[i] = encrypt_nums_to_array_batch(encryptor, batch_encoder,
                                           lr_inputs[i], num_inferences,
                                           slots);
  }
  data_file.close();

  // Server: Run LR regression with ptxt weights
  TIC(auto t1);
  Plaintext twelve("12");
  Plaintext twenty_four("24");
  Plaintext zero("0");
  Ciphertext accum_;
  encryptor.encrypt(zero, accum_);

  // Hypothesis: 
  // Multiply all attributes by the pre-trained weights and accumulate.
  for (size_t i = 0; i < num_attributes; ++i) {
    // ctxt[i] *= Enc(weights[i])
    evaluator.multiply_plain(ctxt_[i], weights[i], ctxt_[i]);
    evaluator.add_inplace(accum_, ctxt_[i]);
  }

  cout << "Hypothesis Noise Check: "
        << decryptor.invariant_noise_budget(accum_) << " bits" << endl;

  // Sigmoid: (1/2 + x/4 + x^3/48)*48 = (24 + 12x + x^3)
  Ciphertext tmp_ = accum_;
  // Calculate x^3.
  evaluator.square_inplace(tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  evaluator.multiply_inplace(tmp_, accum_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  // Calculate 12x.
  evaluator.multiply_plain_inplace(accum_, twelve);
  // Calculate x^3 + 12x.
  evaluator.add_inplace(accum_, tmp_);
  // Calculate x^3 + 12x + 24.
  evaluator.add_plain_inplace(accum_, twenty_four);
  cout << "Sigmoid Noise Check: "
        << decryptor.invariant_noise_budget(accum_) << " bits" << endl;
  auto enc_time_ms = TOC_US(t1);
  cout << "Encrypted execution time " << enc_time_ms << " us" << endl;

  // Client: Decrypt and verify.
  vector<uint64_t> ptxt = decrypt_array_batch_to_nums(decryptor, batch_encoder,
                                                      accum_, slots);
  cout << "Result: \t";
  for (int i = 0; i < num_inferences; ++i) {
    cout << ptxt[i] / scale_factor << " \t";
  }
  cout << endl;
  return EXIT_SUCCESS;
}
