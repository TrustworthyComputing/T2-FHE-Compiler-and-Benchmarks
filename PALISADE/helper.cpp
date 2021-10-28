#include "helper.hpp"

Ciphertext<DCRTPoly> encrypt_repeated_integer(CryptoContext<DCRTPoly>& cc,
                                              LPPublicKey<DCRTPoly>& pk,
                                              int64_t num, size_t n) {
  std::vector<int64_t> v_in(n, num);
  Plaintext pt = cc->MakePackedPlaintext(v_in);
  Ciphertext<DCRTPoly> ct = cc->Encrypt(pk, pt);
  return ct;
}
