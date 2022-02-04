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

typedef enum scheme_t {
  BFV, BGV, CKKS, TFHE, NONE
} scheme_t;

using namespace lbcrypto; 

/// XOR between two batched binary ciphertexts
template <typename T>
Ciphertext<T> exor(CryptoContext<T>& cc, Ciphertext<T>& c1, 
                        Ciphertext<T>& c2) {
  Ciphertext<T> res_ = cc->EvalSub(c1, c2);
  return cc->EvalMultAndRelinearize(res_, res_);;
}

template <typename T>
Ciphertext<T> mux(CryptoContext<T>& cc, Ciphertext<T>& sel, 
                  Ciphertext<T>& c1, Ciphertext<T>& c2) {
  Ciphertext<T> not_sel = cc->EvalNegate(sel);
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  not_sel = cc->EvalAdd(not_sel, pt_one);
  Ciphertext<T> res_ = cc->EvalMultAndRelinearize(c1, sel);
  Ciphertext<T> tmp_ = cc->EvalMultAndRelinearize(c2, not_sel);
  res_ = cc->EvalAdd(res_, tmp_);
  return res_;
}

template <typename T>
Ciphertext<T> eq(CryptoContext<T>& cc, Ciphertext<T>& c1, Ciphertext<T>& c2,
                 size_t ptxt_mod) {
  int num_squares = (int) log2(ptxt_mod-1);
  Ciphertext<T> tmp_ = cc->EvalSub(c1, c2);
  Ciphertext<T> tmp2_ = tmp_->Clone();
  for (int i = 0; i < num_squares; i++) { // Square
    tmp2_ = cc->EvalMultAndRelinearize(tmp2_, tmp2_);
  }
  for (int i = 0; i < (ptxt_mod - 1 - pow(2, num_squares)); i++) { // Mult
    tmp2_ = cc->EvalMultAndRelinearize(tmp2_, tmp_);
  }
  Ciphertext<T> result_ = tmp2_->Clone();
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  Plaintext pt = cc->MakePackedPlaintext(one);
  return cc->EvalSub(pt, result_);
}

template <typename T>
Ciphertext<T> neq(CryptoContext<T>& cc, Ciphertext<T>& c1, Ciphertext<T>& c2,
                 size_t ptxt_mod) {
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  Ciphertext<T> res_ = eq(cc, c1, c2, ptxt_mod);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  res_ = cc->EvalNegate(res_);
  res_ = cc->EvalAdd(res_, pt_one);
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> shift_right_logical_bin(CryptoContext<T>& cc, 
                                           std::vector<Ciphertext<T>>& ct,
                                           size_t amt, LPPublicKey<T>& pub_key) {
  assert(amt < ct.size());
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> zero(slots, 0);
  Plaintext pt_zero = cc->MakePackedPlaintext(zero);
  std::vector<Ciphertext<T>> res_(ct.size());
  // shift data (MSB is at 0, LSB is at size - 1)
  for (int i = (int)ct.size() - amt - 1; i >= 0; --i) {
    res_[i + amt] = ct[i]->Clone();
  }
  // copy sign
  for (int i = amt - 1; i >= 0; --i) {
    res_[i] = cc->Encrypt(pub_key, pt_zero);
  }
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> shift_right_bin(std::vector<Ciphertext<T>>& ct,
                                           size_t amt) {
  assert(amt < ct.size());
  std::vector<Ciphertext<T>> res_(ct.size());
  // shift data (MSB is at 0, LSB is at size - 1)
  for (int i = (int)ct.size() - amt - 1; i >= 0; --i) {
    res_[i + amt] = ct[i]->Clone();
  }
  // copy sign
  for (int i = amt - 1; i >= 0; --i) {
    res_[i] = ct[0]->Clone();
  }
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> shift_left_bin(CryptoContext<T>& cc, 
                                          std::vector<Ciphertext<T>>& ct, 
                                          size_t amt, LPPublicKey<T>& pub_key) {
  assert(amt < ct.size());
  // Initialize with zeros
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> zero(slots, 0);
  Plaintext pt_zero = cc->MakePackedPlaintext(zero);
  std::vector<Ciphertext<T>> res_(ct.size());
  for (size_t i = 0; i < res_.size(); i++) {
    res_[i] = cc->Encrypt(pub_key, pt_zero);
  }
  // shift data (MSB is at 0, LSB is at size - 1)
  for (size_t i = amt; i < ct.size(); ++i) {
    res_[i - amt] = ct[i];
  }
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> mux_bin(CryptoContext<T>& cc, 
                                   std::vector<Ciphertext<T>>& sel, 
                                   std::vector<Ciphertext<T>>& c1, 
                                   std::vector<Ciphertext<T>>& c2) {
  std::vector<Ciphertext<T>> res_(c1.size());
  Ciphertext<T> not_sel = cc->EvalNegate(sel[sel.size()-1]);
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  not_sel = cc->EvalAdd(not_sel, pt_one);
  for (size_t i = 0; i < res_.size(); i++) {
    res_[i] = cc->EvalMultAndRelinearize(c1[i], sel[sel.size()-1]);
    Ciphertext<T> tmp_ = cc->EvalMultAndRelinearize(c2[i], not_sel);
    res_[i] = cc->EvalAdd(res_[i], tmp_);
  }
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> xor_bin(CryptoContext<T>& cc, 
                                   std::vector<Ciphertext<T>>& c1, 
                                   std::vector<Ciphertext<T>>& c2, 
                                   size_t ptxt_mod) {
  assert(c1.size() == c2.size());
  std::vector<Ciphertext<T>> res_(c1.size());
  if (ptxt_mod > 2) {
    // https://stackoverflow.com/a/46674398
    for (size_t i = 0; i < res_.size(); i++) {
      res_[i] = cc->EvalSub(c1[i], c2[i]);
      res_[i] = cc->EvalMultAndRelinearize(res_[i], res_[i]);
    }
  } else {
    for (size_t i = 0; i < res_.size(); i++) {
      res_[i] = cc->EvalAdd(c1[i], c2[i]);
    }
  }
  return res_;
}

template <typename T>
Ciphertext<T> lt(CryptoContext<T>& cc, Ciphertext<T>& c1, Ciphertext<T>& c2,
                 LPPublicKey<T>& pub_key, size_t ptxt_mod) {
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  Ciphertext<T> tmp_, tmp2_, result_;
  std::vector<int64_t> zero(slots, 0);
  Plaintext pt_zero = cc->MakePackedPlaintext(zero);
  result_ = cc->Encrypt(pub_key, pt_zero);
  int num_squares = (int) log2(ptxt_mod-1);
  for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
    std::vector<int64_t> a_vec(slots, i);
    Plaintext a = cc->MakePackedPlaintext(a_vec);
    tmp_ = cc->EvalSub(c1, c2);
    tmp_ = cc->EvalSub(tmp_, a);
    tmp2_ = tmp_->Clone();
    for (int j = 0; j < num_squares; j++) { // Square
      tmp2_ = cc->EvalMultAndRelinearize(tmp2_, tmp2_);
    }
    for (int j = 0; j < (ptxt_mod - 1 - pow(2, num_squares)); j++) { // Mult
      tmp2_ = cc->EvalMultAndRelinearize(tmp2_, tmp_);
    }
    tmp_ = cc->EvalSub(pt_one, tmp2_);
    result_ = cc->EvalAdd(result_, tmp_);
  }
  return result_;
}

template <typename T>
Ciphertext<T> leq(CryptoContext<T>& cc, Ciphertext<T>& c1, Ciphertext<T>& c2,
                  LPPublicKey<T>& pub_key, size_t ptxt_mod) {
  Ciphertext<T> res_ = lt(cc, c2, c1, pub_key, ptxt_mod);
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  return cc->EvalSub(pt_one, res_);
}

template <typename T>
std::vector<Ciphertext<T>> eq_bin(CryptoContext<T>& cc, 
                                  std::vector<Ciphertext<T>>& c1, 
                                  std::vector<Ciphertext<T>>& c2,
                                  LPPublicKey<T>& pub_key) {
  size_t slots(cc->GetRingDimension());
  size_t word_sz = c1.size();
  std::vector<Ciphertext<T>> res_(word_sz);
  Ciphertext<T> tmp_, tmp_res_;
  std::vector<int64_t> one(slots, 1), zero(slots, 0);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  Plaintext pt_zero = cc->MakePackedPlaintext(zero);
  for (int i = word_sz - 1; i >= 0; i--) {
    tmp_res_ = cc->Encrypt(pub_key, pt_one);
    tmp_ = cc->EvalSub(c1[i], c2[i]);
    tmp_ = cc->EvalMultAndRelinearize(tmp_, tmp_);
    tmp_res_ = cc->EvalSub(tmp_res_, tmp_);
    if (i == (int) word_sz - 1) {
      res_[word_sz - 1] = tmp_res_->Clone();
    } else {
      res_[word_sz - 1] = cc->EvalMultAndRelinearize(tmp_res_, res_[word_sz - 1]);
      res_[i] = cc->Encrypt(pub_key, pt_zero);
    }
  }
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> neq_bin(CryptoContext<T>& cc, 
                                  std::vector<Ciphertext<T>>& c1, 
                                  std::vector<Ciphertext<T>>& c2,
                                  LPPublicKey<T>& pub_key) {
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  std::vector<Ciphertext<T>> res_ = eq_bin(cc, c1, c2, pub_key);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  res_[res_.size()-1] = cc->EvalNegate(res_[res_.size()-1]);
  res_[res_.size()-1] = cc->EvalAdd(res_[res_.size()-1], pt_one);
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> slice(
    std::vector<Ciphertext<T>>& in_, size_t start, size_t end) {
  std::vector<Ciphertext<T>> res_(end-start);
  for (size_t i = start; i < end; i++) {
    res_[i-start] = in_[i]->Clone();
  }
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> lt_bin(CryptoContext<T>& cc, 
                                  std::vector<Ciphertext<T>>& c1, 
                                  std::vector<Ciphertext<T>>& c2,
                                  size_t word_sz,
                                  LPPublicKey<T>& pub_key) {
  size_t slots(cc->GetRingDimension());
  std::vector<Ciphertext<T>> res_(word_sz);
  if (c1.size() == 1) {
    std::vector<int64_t> one(slots, 1);
    Plaintext pt_one = cc->MakePackedPlaintext(one);
    Ciphertext<T> c1_neg = cc->EvalNegate(c1[0]);
    c1_neg = cc->EvalAdd(c1_neg, pt_one);
    res_[word_sz - 1] = cc->EvalMultAndRelinearize(c1_neg, c2[0]);
    return res_;
  }
  int len = c1.size() >> 1;
  std::vector<Ciphertext<T>> lhs_h = slice(c1, 0, len);
  std::vector<Ciphertext<T>> lhs_l = slice(c1, len, c1.size());
  std::vector<Ciphertext<T>> rhs_h = slice(c2, 0, len);
  std::vector<Ciphertext<T>> rhs_l = slice(c2, len, c1.size());
  std::vector<Ciphertext<T>> term1 = lt_bin(cc, lhs_h, rhs_h, word_sz, pub_key);
  std::vector<Ciphertext<T>> h_equal = eq_bin(cc, lhs_h, rhs_h, pub_key);
  std::vector<Ciphertext<T>> l_equal = lt_bin(cc, lhs_l, rhs_l, word_sz, pub_key);
  Ciphertext<T> term2 = cc->EvalMultAndRelinearize(h_equal[lhs_h.size() - 1],
      l_equal[word_sz - 1]);
  res_[word_sz - 1] = exor(cc, term1[word_sz - 1], term2);
  std::vector<int64_t> vzero(slots, 0);
  Plaintext pt_zero = cc->MakePackedPlaintext(vzero);
  for (size_t i = 0; i < word_sz - 1; i++) {
    res_[i] = cc->Encrypt(pub_key, pt_zero); // Pad result with 0's
  }
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> leq_bin(CryptoContext<T>& cc, 
                                   std::vector<Ciphertext<T>>& c1, 
                                   std::vector<Ciphertext<T>>& c2, 
                                   LPPublicKey<T>& pub_key) {
  std::vector<Ciphertext<T>> res_ = lt_bin(cc, c2, c1, c1.size(), pub_key);
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  res_[c1.size() - 1] = cc->EvalSub(pt_one, res_[c1.size() - 1]);
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> dec_bin(CryptoContext<T>& cc, 
                                   std::vector<Ciphertext<T>>& c1, 
                                   LPPublicKey<T>& pub_key) {
  size_t slots(cc->GetRingDimension());
  size_t word_sz = c1.size();
  std::vector<int64_t> one(slots, 1);
  Plaintext carry_ptxt = cc->MakePackedPlaintext(one);
  Ciphertext<T> carry_, neg_c1;
  carry_ = cc->Encrypt(pub_key, carry_ptxt);
  std::vector<Ciphertext<T>> res_(word_sz);
  for (int i = word_sz - 1; i > 0; --i) {
    res_[i] = exor(cc, c1[i], carry_);
    neg_c1 = cc->EvalNegate(c1[i]);
    neg_c1 = cc->EvalAdd(neg_c1, carry_ptxt);
    carry_ = cc->EvalMultAndRelinearize(neg_c1, carry_);
  }
  res_[0] = exor(cc, c1[0], carry_);
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> inc_bin(CryptoContext<T>& cc, 
                                   std::vector<Ciphertext<T>>& c1, 
                                   LPPublicKey<T>& pub_key) {
  size_t slots(cc->GetRingDimension());
  size_t word_sz = c1.size();
  std::vector<int64_t> one(slots, 1);
  Plaintext carry_ptxt = cc->MakePackedPlaintext(one);
  Ciphertext<T> carry_;
  carry_ = cc->Encrypt(pub_key, carry_ptxt);
  std::vector<Ciphertext<T>> res_(word_sz);
  for (int i = word_sz - 1; i > 0; --i) {
    res_[i] = exor(cc, c1[i], carry_);
    carry_ = cc->EvalMultAndRelinearize(c1[i], carry_);
  }
  res_[0] = exor(cc, c1[0], carry_);
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> add_bin(CryptoContext<T>& cc, 
                                   std::vector<Ciphertext<T>>& c1, 
                                   std::vector<Ciphertext<T>>& c2, 
                                   LPPublicKey<T>& pub_key) {
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> zero(slots, 0);
  Plaintext carry_ptxt = cc->MakePackedPlaintext(zero);
  Ciphertext<T> carry_;
  carry_ = cc->Encrypt(pub_key, carry_ptxt);
  std::vector<Ciphertext<T>>& smaller = (c1.size() < c2.size()) ? c1 : c2;
  std::vector<Ciphertext<T>>& bigger = (c1.size() < c2.size()) ? c2 : c1;
  size_t offset = bigger.size() - smaller.size();
  std::vector<Ciphertext<T>> res_(smaller.size());
  for (int i = smaller.size()-1; i >= 0; --i) {
    // sum = (ct1_ ^ ct2_) ^ in_carry
    Ciphertext<T> xor_ = exor(cc, smaller[i], bigger[i + offset]);
    res_[i] = exor(cc, xor_, carry_);
    if (i == 0) break; // don't need output carry
    // next carry computation
    Ciphertext<T> prod_;
    prod_ = cc->EvalMultAndRelinearize(smaller[i], bigger[i + offset]);
    xor_ = cc->EvalMultAndRelinearize(carry_, xor_);
    carry_ = exor(cc, prod_, xor_);
  }
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> sub_bin(CryptoContext<T>& cc, 
                                   std::vector<Ciphertext<T>>& c1, 
                                   std::vector<Ciphertext<T>>& c2, 
                                   LPPublicKey<T>& pub_key) {
  assert(c1.size() == c2.size());
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> zero(slots, 0);
  Plaintext carry_ptxt = cc->MakePackedPlaintext(zero);
  Ciphertext<T> carry_;
  carry_ = cc->Encrypt(pub_key, carry_ptxt);
  std::vector<int64_t> one(slots, 1);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  std::vector<Ciphertext<T>> res_(c1.size());
  // Generate two's complement of ct2_
  std::vector<Ciphertext<T>> neg_c2(c2.size());
  for (int i = c2.size()-1; i > -1; --i) {
    neg_c2[i] = cc->EvalNegate(c2[i]);
    neg_c2[i] = cc->EvalAdd(neg_c2[i], one);
  }
  neg_c2 = inc_bin(cc, neg_c2, pub_key);
  for (int i = c1.size()-1; i >= 0; --i) {
    // sum = (ct1_ ^ ct2_) ^ in_carry
    Ciphertext<T> xor_ = exor(cc, c1[i], neg_c2[i]);
    res_[i] = exor(cc, xor_, carry_);
    if (i == 0) break; // don't need output carry
    // next carry computation
    Ciphertext<T> prod_;
    prod_ = cc->EvalMultAndRelinearize(c1[i], neg_c2[i]);
    xor_ = cc->EvalMultAndRelinearize(carry_, xor_);
    carry_ = exor(cc, prod_, xor_);
  }
  return res_;
}

template <typename T>
std::vector<Ciphertext<T>> mult_bin(CryptoContext<T>& cc, 
                                    std::vector<Ciphertext<T>>& c1, 
                                    std::vector<Ciphertext<T>>& c2, 
                                    LPPublicKey<T>& pub_key) {
  assert(c1.size() == c2.size());
  std::vector<Ciphertext<T>> tmp_(c1.size());
  std::vector<Ciphertext<T>> prod_(c1.size());
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> zero(slots, 0);
  Plaintext pt_zero = cc->MakePackedPlaintext(zero);
  for (int i = 0; i < prod_.size(); i++) {
    prod_[i] = cc->Encrypt(pub_key, pt_zero);
  }
  for (int i = c1.size()-1; i >= 0; i--) {
    for (int j = c2.size()-1; j >= (int)c2.size()-1-i; --j) {
      tmp_[j] = cc->EvalMultAndRelinearize(c1[i], c2[j]);
    }
    std::vector<Ciphertext<T>> tmp_slice_ = slice(prod_, 0, i+1);
    tmp_slice_ = add_bin(cc, tmp_slice_, tmp_, pub_key);
    for (int j = i; j >= 0; --j) {
      prod_[j] = tmp_slice_[j];
    }
  }
  return prod_;
}

template <typename T>
std::vector<Ciphertext<T>> not_bin(CryptoContext<T>& cc, 
                                    std::vector<Ciphertext<T>>& ct) {
  std::vector<Ciphertext<T>> res_(ct.size());
  size_t slots(cc->GetRingDimension());
  std::vector<int64_t> one(slots, 1);
  Plaintext pt_one = cc->MakePackedPlaintext(one);
  for (size_t i = 0; i < res_.size(); i++) {
    res_[i] = cc->EvalNegate(ct[i]);
    res_[i] = cc->EvalAdd(res_[i], pt_one);
  }
  return res_;
}

#endif  // FUNCTIONAL_UNITS_HPP_
