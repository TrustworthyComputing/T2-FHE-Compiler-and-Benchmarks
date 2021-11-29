#include "../../helper.hpp"

using namespace std;
using namespace lbcrypto;

#define scale_factor 48

int main(int argc, char** argv) {
  // Argument sanity checks.
  std::ifstream data_file, weights_file;
  if (argc < 5) {
    cerr << "Usage: " << argv[0] <<
      " data_filename weights_filename plaintext_modulus scheme" << endl <<
      "\tdata_filename: Path to the data to be classified" << endl <<
      "\tweights_filename: Path to the pre-trained LR weights" << endl <<
      "\tplaintext_modulus: range of plaintext values" << endl <<
      "\tscheme: bfv, bgv" << endl;
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
  // size_t plaintext_modulus = atol(argv[3]); // 786433
  PlaintextModulus plaintext_modulus = strtoull(argv[3  ], NULL, 0); //
  scheme_t scheme = NONE;
  if (strcmp(argv[4], "bfv") == 0) {
    scheme = BFV;
  } else if (strcmp(argv[4], "bgv") == 0) {
    scheme = BGV;
  } else {
    cerr << "Choose either bgv or bfv" << endl;
    return EXIT_FAILURE;
  }

  // TODO(@jimouris): Figure out a correct depth value.
  uint32_t depth = 10;
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

  unsigned int slots = cc->GetEncodingParams()->GetBatchSize();
  // Client: Key generation.
  auto keyPair = cc->KeyGen();
  // Generate the relinearization key.
  cc->EvalMultKeyGen(keyPair.secretKey);

  // Client: Read plaintext weights and encrypt classification inputs.
  size_t num_attributes = 2;
  size_t num_inferences = 1;

  // Read and encrypt data.
  data_file >> num_inferences;
  data_file >> num_attributes;
  // Batching: Multiple classifications at once would yield higher throughput.
  vector<Ciphertext<DCRTPoly>> ctxt_(num_attributes);
  assert(slots > num_inferences);
  vector<vector<int64_t>> lr_inputs(num_attributes,
                                    vector<int64_t>(num_inferences, 0));
  double curr_input = 0.0;
  for (size_t i = 0; i < num_inferences; ++i) {
    for (size_t j = 0; j < num_attributes; ++j) {
      data_file >> curr_input;
      lr_inputs[j][i] = static_cast<int64_t>(curr_input * scale_factor);
    }
  }
  for (size_t i = 0; i < num_attributes; i++) {
    Plaintext pt = cc->MakePackedPlaintext(lr_inputs[i]);
    ctxt_[i] = cc->Encrypt(keyPair.publicKey, pt);
  }
  data_file.close();

  // Read plaintext weights.
  weights_file >> num_attributes;
  vector<Plaintext> weights(num_attributes);
  for (size_t i = 0; i < num_attributes; i++) {
    weights_file >> curr_input;
    vector<int64_t> tmpvec(num_inferences,
                           static_cast<int64_t>(curr_input * scale_factor));
    weights[i] = cc->MakePackedPlaintext(tmpvec);

    cout << "w[" << i << "] : " << weights[i] << endl;
  }
  weights_file.close();

  // Server: Run LR regression with ptxt weights
  TIC(auto t1);
  auto twelve = cc->MakePackedPlaintext({ 12 });
  auto twenty_four = cc->MakePackedPlaintext({ 24 });
  auto zero = cc->MakePackedPlaintext({ 0 });
  auto accum_ = cc->Encrypt(keyPair.publicKey, zero);

  // Hypothesis: 
  // Multiply all attributes by the pre-trained weights and accumulate.
  for (size_t i = 0; i < num_attributes; ++i) {
    // ctxt_[i] *= weights[i]
    ctxt_[i] = cc->EvalMult(weights[i], ctxt_[i]);

    Plaintext packed_result_ptxt_1;
    cc->Decrypt(keyPair.secretKey, ctxt_[i], &packed_result_ptxt_1);
    packed_result_ptxt_1->SetLength(num_inferences);
    cout << i << ") " << packed_result_ptxt_1 << endl;

    // accum_ += ctxt_[i]
    accum_ = cc->EvalAdd(accum_, ctxt_[i]);
  }
  
  // Sigmoid: (1/2 + x/4 + x^3/48)*48 = (24 + 12x + x^3)
  auto tmp_ = accum_;
  // Calculate x^3.
  tmp_ = cc->EvalMultAndRelinearize(tmp_, tmp_);
  tmp_ = cc->EvalMultAndRelinearize(tmp_, accum_);
  // Calculate 12x.
  accum_ = cc->EvalMult(twelve, accum_);
  // Calculate x^3 + 12x.
  accum_ = cc->EvalAdd(accum_, tmp_);
  // Calculate x^3 + 12x + 24.
  accum_ = cc->EvalAdd(twenty_four, accum_);
  auto enc_time_ms = TOC_US(t1);
  cout << "Encrypted execution time " << enc_time_ms << " us" << endl;

  // Client: Decrypt and verify.
  Plaintext packed_result_ptxt;
  cc->Decrypt(keyPair.secretKey, accum_, &packed_result_ptxt);
  packed_result_ptxt->SetLength(num_inferences);
  // cout << "Result: \t" << packed_result_ptxt << endl;

  vector<int64_t> res_vec = packed_result_ptxt->GetPackedValue();
  cout << "Result: \t";
  for (auto v : res_vec) {
    cout << static_cast<uint64_t>(v) / scale_factor << " \t";
  }
  cout << endl;

  return EXIT_SUCCESS;
}
