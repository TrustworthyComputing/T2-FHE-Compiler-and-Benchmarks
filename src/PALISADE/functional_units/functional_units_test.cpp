#include "functional_units.hpp"

using namespace lbcrypto;
using namespace std;

// 2m | p - 1
int main() {
  size_t plaintext_modulus = 257;

  double sigma = 3.2;
  // SecurityLevel securityLevel = HEStd_128_classic;
  size_t depth = 14;
  CryptoContext<DCRTPoly> cc;
  cc = CryptoContextFactory<DCRTPoly>::genCryptoContextBFVrns(
        plaintext_modulus, 80.0, sigma, 0, depth, 0, OPTIMIZED, depth, 0, 60, 128);
  cc->Enable(ENCRYPTION);
  cc->Enable(SHE);

  auto keyPair = cc->KeyGen();
  cc->EvalMultKeyGen(keyPair.secretKey);

  size_t slots(cc->GetRingDimension());
  vector<int64_t> tmpvec(slots);
  for (size_t i = 0; i < slots; i++) {
    tmpvec[i] = i;
  }
  auto tmp = cc->MakePackedPlaintext(tmpvec);
  auto ct1_ = cc->Encrypt(keyPair.publicKey, tmp);

  vector<int64_t> tmpvec_2(slots);
  for (size_t i = 0; i < slots; i++) {
    tmpvec_2[i] = i;
  }
  tmpvec_2[5] = 0;
  auto tmp_2 = cc->MakePackedPlaintext(tmpvec_2);
  auto ct2_ = cc->Encrypt(keyPair.publicKey, tmp_2);

  Ciphertext<DCRTPoly> res_ = lt(cc, ct2_, ct1_, keyPair.publicKey,
  plaintext_modulus);
  
  // Ciphertext<DCRTPoly> res_ = eq(cc, ct2_, ct1_, plaintext_modulus);

  // Ciphertext<DCRTPoly> res_ = cc->EvalAdd(ct1_, ct2_);

  cout << ct1_->GetLevel() << endl;


  Plaintext result;
  cc->Decrypt(keyPair.secretKey, res_, &result);
  for (size_t i = 0; i < slots; i++) {
    cout << result->GetPackedValue()[i] << " ";
  }
  cout << endl;

  // Ciphertext<DCRTPoly> res_ = exor(cc, ct1_, ct1_);

  // Ciphertext<DCRTPoly> res_ = eq(cc, ct1_, ct2_, plaintext_modulus);

  // Plaintext result;
  // cc->Decrypt(keyPair.secretKey, res_, &result);
  // for (size_t i = 0; i < slots; i++) {
  //   assert(result->GetPackedValue()[i] == 0);
  // }

  // for (size_t i = 0; i < slots; i++) {
  //   tmpvec_2[i] = i;
  // }
  // tmp_2 = cc->MakePackedPlaintext(tmpvec_2);
  // ct2_ = cc->Encrypt(keyPair.publicKey, tmp_2);


  // res_ = eq(cc, ct1_, ct2_, plaintext_modulus);
  // cc->Decrypt(keyPair.secretKey, res_, &result);
  // for (size_t i = 0; i < slots; i++) {
  //   assert(result->GetPackedValue()[i] == 1);
  // }

  // for (size_t i = 0; i < slots; i++) {
  //   tmpvec_2[i] = i;
  // }
  // tmpvec_2[1] = 0;
  // tmp_2 = cc->MakePackedPlaintext(tmpvec_2);
  // ct2_ = cc->Encrypt(keyPair.publicKey, tmp_2);

  // // res_ = eq(cc, ct1_, ct2_, plaintext_modulus);
  // cc->Decrypt(keyPair.secretKey, res_, &result);
  // for (size_t i = 0; i < slots; i++) {
  //   cout << result->GetPackedValue()[i] << " ";
  // }
  // cout << endl;

  return EXIT_SUCCESS;
}
