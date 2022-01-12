#ifndef FUNCTIONAL_UNITS_HPP_
#define FUNCTIONAL_UNITS_HPP_

#include "palisade.h"
#include <algorithm>
#include <chrono>
#include <cstddef>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <limits>
#include <memory>
#include <mutex>
#include <math.h>
#include <numeric>
#include <random>
#include <sstream>
#include <string>
#include <thread>
#include <vector>
#include <cassert>

#define duration(a) \
  std::chrono::duration_cast<std::chrono::milliseconds>(a).count()
#define duration_ns(a) \
  std::chrono::duration_cast<std::chrono::nanoseconds>(a).count()
#define duration_us(a) \
  std::chrono::duration_cast<std::chrono::microseconds>(a).count()
#define duration_ms(a) \
  std::chrono::duration_cast<std::chrono::milliseconds>(a).count()
#define timeNow() std::chrono::high_resolution_clock::now()

#define TIC(t) t = timeNow()
#define TOC(t) duration(timeNow() - t)
#define TOC_NS(t) duration_ns(timeNow() - t)
#define TOC_US(t) duration_us(timeNow() - t)
#define TOC_MS(t) duration_ms(timeNow() - t)

typedef enum scheme_t {
  BFV, BGV, CKKS, TFHE, NONE
} scheme_t;

using namespace lbcrypto; 

/// Helper function to encrypt an integer repeatedly into a packed plaintext
template <typename T>
Ciphertext<T> encrypt_repeated_integer(CryptoContext<T>& cc, LPPublicKey<T>& pk,
                                       int64_t num, size_t n) {
  std::vector<int64_t> v_in(n, num);
  Plaintext pt = cc->MakePackedPlaintext(v_in);
  Ciphertext<T> ct = cc->Encrypt(pk, pt);
  return ct;

}

/// XOR between two batched binary ciphertexts
template <typename T>
Ciphertext<T> xor_batch(CryptoContext<T>& cc, Ciphertext<T>& ctxt_1, 
                        Ciphertext<T>& ctxt_2) {
  Ciphertext<T> res_ = cc->EvalSub(ctxt_1, ctxt_2);
  res_ = cc->EvalMultAndRelinearize(res_, res_);
  return res_;
}

template <typename T>
Ciphertext<T> eq(CryptoContext<T>& cc, Ciphertext<T>& ct1_, Ciphertext<T>& ct2_, size_t ptxt_mod) {
  int num_squares = (int)log2(ptxt_mod-1);
  Ciphertext<T> tmp_ = cc->EvalSub(ct1_, ct2_);
  Ciphertext<T> tmp2_ = tmp_->Clone();
  for (int i = 0; i < num_squares; i++) { // Square
    tmp2_ = cc->EvalMultAndRelinearize(tmp2_, tmp2_);
  }
  for (int i = 0; i < ((ptxt_mod-1) - pow(2,num_squares)); i++) { // Mult
    tmp2_ = cc->EvalMultAndRelinearize(tmp2_, tmp_);
  }
  Ciphertext<T> result_ = tmp2_->Clone();
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  Plaintext pt = cc->MakePackedPlaintext(one);
  result_ = cc->EvalSub(pt, result_);
  return result_;
}

template <typename T>
Ciphertext<T> lt(CryptoContext<T>& cc, Ciphertext<T>& ct1_, Ciphertext<T>& ct2_,
                 LPPublicKey<T>& pub_key, size_t ptxt_mod) {
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  Ciphertext<T> tmp_, tmp2_, result_;
  std::vector<int64_t> zero(slots, 0);
  Plaintext pt_zero = cc->MakePackedPlaintext(zero);
  result_ = cc->Encrypt(pub_key, pt_zero);

  int num_squares = (int)log2(ptxt_mod-1);
  for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
    std::vector<int64_t> a_vec(slots, i);
    Plaintext a = cc->MakePackedPlaintext(a_vec);
    tmp_ = cc->EvalSub(ct1_, ct2_);
    tmp_ = cc->EvalSub(tmp_, a);
    tmp2_ = tmp_->Clone();
    for (int j = 0; j < num_squares; j++) { // Square
      tmp2_ = cc->EvalMultAndRelinearize(tmp2_, tmp2_);
    }
    for (int j = 0; j < ((ptxt_mod-1) - pow(2,num_squares)); j++) { // Mult
      tmp2_ = cc->EvalMultAndRelinearize(tmp2_, tmp_);
    }
    tmp_ = cc->EvalSub(pt_one, tmp2_);
    result_ = cc->EvalAdd(result_, tmp_);
  }
  return result_;
}

template <typename T>
Ciphertext<T> leq(CryptoContext<T>& cc, Ciphertext<T>& ct1_, Ciphertext<T>& ct2_,
                  LPPublicKey<T>& pub_key, size_t ptxt_mod) {
  Ciphertext<T> res_ = lt(cc, ct2_, ct1_, pub_key, ptxt_mod);
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  res_ = cc->EvalSub(pt_one, res_);
  return res_;
}

#endif  // FUNCTIONAL_UNITS_HPP_
