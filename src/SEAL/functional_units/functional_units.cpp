#include "functional_units.hpp"

seal::Ciphertext encrypt_all_slots(
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder,
    uint64_t num, size_t slots) {
  std::vector<uint64_t> vec_num(slots);
  for (int i = 0; i < vec_num.size(); ++i) {
    vec_num[i] = num;
  }
  seal::Plaintext vec_num_encoded;
  batch_encoder.encode(vec_num, vec_num_encoded);
  seal::Ciphertext res_;
  encryptor.encrypt(vec_num_encoded, res_);
  return res_;
}

seal::Plaintext encode_all_slots(
  seal::BatchEncoder& batch_encoder, uint64_t num, size_t slots) {
  std::vector<uint64_t> vec_num(slots, num);
  seal::Plaintext vec_num_encoded;
  batch_encoder.encode(vec_num, vec_num_encoded);
  return vec_num_encoded;
}

seal::Ciphertext mult_all_slots(
    seal::Evaluator& evaluator, seal::Ciphertext& c1, size_t slots,
    size_t padding, seal::GaloisKeys& galois_keys) {
  seal::Ciphertext res = c1;
  for (int i = 0; i < slots; i++) {
    evaluator.rotate_rows_inplace(res, padding, galois_keys);
    evaluator.multiply(res, c1, res);
  }
  return res;
}

std::vector<uint64_t> decrypt_array_batch_to_nums(
    seal::Decryptor& decryptor, seal::BatchEncoder& batch_encoder,
    seal::Ciphertext& encrypted_vec, size_t slots, size_t padding /* = 1 */) {
  seal::Plaintext ptxt_vec_encoded;
  decryptor.decrypt(encrypted_vec, ptxt_vec_encoded);
  std::vector<uint64_t> ptxt_vec;
  batch_encoder.decode(ptxt_vec_encoded, ptxt_vec);
  std::vector<uint64_t> result_vec(slots / padding);
  for (int i = 0; i < slots/padding; ++i) {
    result_vec[i] = ptxt_vec[i * padding];
  }
  return result_vec;
}

std::vector<seal::Ciphertext> not_bin(seal::Evaluator& evaluator, 
                            seal::BatchEncoder& batch_encoder, 
                            std::vector<seal::Ciphertext>& ct_, size_t slots) {
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  std::vector<seal::Ciphertext> res_(ct_.size());
  for (int i = 0; i < ct_.size(); i++) {
    evaluator.negate(ct_[i], res_[i]);
    evaluator.add_plain(res_[i], one, res_[i]);
  }
  return res_;
}

seal::Ciphertext exor(seal::Ciphertext& ctxt_1, seal::Ciphertext& ctxt_2,
                           seal::Evaluator& evaluator,
                           seal::Encryptor& encryptor,
                           const seal::RelinKeys& relinKeys) {
  // avoid transparent ciphertext warning
  seal::Ciphertext tmp;
  seal::Plaintext zero("0");
  encryptor.encrypt(zero, tmp);
  evaluator.add(tmp, ctxt_2, tmp);
  // https://stackoverflow.com/a/46674398
  seal::Ciphertext result;
  evaluator.sub(ctxt_1, tmp, result);
  evaluator.square_inplace(result);
  evaluator.relinearize_inplace(result, relinKeys);
  return result;
}

seal::Ciphertext mux(seal::Evaluator& evaluator, 
                     seal::RelinKeys& relin_keys, seal::Ciphertext& sel, 
                     seal::Ciphertext& ctxt_1,  seal::Ciphertext& ctxt_2,
                     size_t slots) {
  seal::Ciphertext res_, not_sel, tmp;
  evaluator.negate(sel, not_sel);
  seal::Plaintext one("1");
  evaluator.add_plain(not_sel, one, not_sel);
  evaluator.multiply(ctxt_1, sel, res_);
  evaluator.relinearize_inplace(res_, relin_keys);
  evaluator.multiply(ctxt_2, not_sel, tmp);
  evaluator.relinearize_inplace(tmp, relin_keys);
  evaluator.add(res_, tmp, res_);
  return res_;
}


std::vector<seal::Ciphertext> shift_right_bin(std::vector<seal::Ciphertext>& ct,
                                              size_t amt, size_t slots) {
  assert(amt < ct.size());
  std::vector<seal::Ciphertext> res_(ct.size());  
  // shift data (MSB is at 0, LSB is at size - 1)
  for (int i = ct.size() - amt - 1; i >= 0; --i) {
    res_[i + amt] = ct[i];
  }
  // copy sign
  for (int i = amt - 1; i >= 0; --i) {
    res_[i] = ct[0];
  }
  return res_;
}

std::vector<seal::Ciphertext> shift_right_logical_bin(seal::Encryptor& encryptor,
                                              seal::BatchEncoder& batch_encoder,
                                              std::vector<seal::Ciphertext>& ct,
                                              size_t amt, size_t slots) {
  assert(amt < ct.size());
  seal::Plaintext zero = encode_all_slots(batch_encoder, 0, slots);
  std::vector<seal::Ciphertext> res_(ct.size());  
  // shift data (MSB is at 0, LSB is at size - 1)
  for (int i = ct.size() - amt - 1; i >= 0; --i) {
    res_[i + amt] = ct[i];
  }
  // shift in zeros
  for (int i = amt - 1; i >= 0; --i) {
    encryptor.encrypt(zero, res_[i]);
  }
  return res_;
}

std::vector<seal::Ciphertext> shift_left_bin(seal::Encryptor& encryptor, 
                                             seal::BatchEncoder& batch_encoder,
                                             std::vector<seal::Ciphertext>& ct, 
                                             size_t amt, size_t slots) {
  assert(amt < ct.size());
  // Initialize with zeros
  seal::Plaintext zero = encode_all_slots(batch_encoder, 0, slots);
  std::vector<seal::Ciphertext> res_(ct.size());  
  for (int i = 0; i < res_.size(); i++) {
    encryptor.encrypt(zero, res_[i]);
  }
  // shift data (MSB is at 0, LSB is at size - 1)
  for (int i = amt; i < ct.size(); ++i) {
    res_[i - amt] = ct[i];
  }
  return res_;
}

std::vector<seal::Ciphertext> xor_bin(seal::Evaluator& evaluator,
                                      seal::Encryptor& encryptor,
                                      seal::RelinKeys& relinKeys, 
                                      std::vector<seal::Ciphertext>& ctxt_1, 
                                      std::vector<seal::Ciphertext>& ctxt_2,
                                      size_t ptxt_mod) {
  assert(ctxt_1.size() == ctxt_2.size());
  std::vector<seal::Ciphertext> res_(ctxt_1.size());
  seal::Ciphertext tmp;
  seal::Plaintext zero("0");
  if (ptxt_mod > 2) {
    // https://stackoverflow.com/a/46674398
    for (int i = 0; i < res_.size(); i++) {
      // avoid transparent ciphertext warning
      encryptor.encrypt(zero, tmp);
      evaluator.add(tmp, ctxt_2[i], tmp);
      evaluator.sub(ctxt_1[i], tmp, res_[i]);
      evaluator.square_inplace(res_[i]);
      evaluator.relinearize_inplace(res_[i], relinKeys);
    }
  } else {
    for (int i = 0; i < res_.size(); i++) {
      evaluator.add(ctxt_1[i], ctxt_2[i], res_[i]);
    }
  }
  return res_;
}

std::vector<seal::Ciphertext> and_bin(seal::Evaluator& evaluator,
                                      seal::Encryptor& encryptor,
                                      seal::RelinKeys& relinKeys, 
                                      std::vector<seal::Ciphertext>& ctxt_1, 
                                      std::vector<seal::Ciphertext>& ctxt_2,
                                      size_t ptxt_mod) {
  assert(ctxt_1.size() == ctxt_2.size());
  std::vector<seal::Ciphertext> res_(ctxt_1.size());
  // avoid transparent ciphertext warning
  seal::Ciphertext tmp;
  seal::Plaintext zero("0");
  for (int i = 0; i < res_.size(); i++) {
    encryptor.encrypt(zero, tmp);
    evaluator.add(tmp, ctxt_2[i], tmp);
    // https://stackoverflow.com/a/46674398
    evaluator.multiply(ctxt_1[i], tmp, res_[i]);
    evaluator.relinearize_inplace(res_[i], relinKeys);
  }
  return res_;
}

std::vector<seal::Ciphertext> or_bin(seal::Evaluator& evaluator,
                                      seal::Encryptor& encryptor,
                                      seal::RelinKeys& relinKeys, 
                                      std::vector<seal::Ciphertext>& ctxt_1, 
                                      std::vector<seal::Ciphertext>& ctxt_2,
                                      size_t ptxt_mod) {
  assert(ctxt_1.size() == ctxt_2.size());
  std::vector<seal::Ciphertext> res_(ctxt_1.size());
  // avoid transparent ciphertext warning
  seal::Ciphertext tmp, tmp2;
  seal::Plaintext zero("0");
  for (int i = 0; i < res_.size(); i++) {
    encryptor.encrypt(zero, tmp);
    evaluator.add(tmp, ctxt_2[i], tmp);
    // https://stackoverflow.com/a/46674398
    evaluator.multiply(ctxt_1[i], tmp, tmp2);
    evaluator.relinearize_inplace(tmp2, relinKeys);
    evaluator.add(tmp, ctxt_1[i], tmp);
    evaluator.sub(tmp, tmp2, res_[i]);
  }
  return res_;
}

std::vector<seal::Ciphertext> mux_bin(seal::Evaluator& evaluator,
                                      seal::BatchEncoder& batch_encoder,
                                      seal::RelinKeys& relin_keys, 
                                      std::vector<seal::Ciphertext>& sel, 
                                      std::vector<seal::Ciphertext>& ctxt_1, 
                                      std::vector<seal::Ciphertext>& ctxt_2,
                                      size_t slots) {
  std::vector<seal::Ciphertext> res_(ctxt_1.size());
  seal::Ciphertext not_sel, tmp;
  evaluator.negate(sel[sel.size()-1], not_sel);
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  evaluator.add_plain(not_sel, one, not_sel);
  for (int i = 0; i < res_.size(); i++) {
    evaluator.multiply(ctxt_1[i], sel[sel.size()-1], res_[i]);
    evaluator.relinearize_inplace(res_[i], relin_keys);
    evaluator.multiply(ctxt_2[i], not_sel, tmp);
    evaluator.relinearize_inplace(tmp, relin_keys);
    evaluator.add(res_[i], tmp, res_[i]);
  }
  return res_;
}

// Vertical batching (assume all slots are independent) (XNOR)
std::vector<seal::Ciphertext> eq_bin(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, seal::RelinKeys& relin_keys, 
    std::vector<seal::Ciphertext>& c1, std::vector<seal::Ciphertext>& c2, 
    size_t word_sz, size_t slots) {
  std::vector<seal::Ciphertext> res_(word_sz);
  seal::Ciphertext tmp_, tmp_res_;
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  seal::Plaintext zero = encode_all_slots(batch_encoder, 0, slots);
  for (int i = word_sz-1; i > -1; i--) {
    encryptor.encrypt(one, tmp_res_);
    encryptor.encrypt(zero, tmp_);
    evaluator.add(tmp_, c2[i], tmp_);
    evaluator.sub(c1[i], tmp_, tmp_);
    evaluator.square_inplace(tmp_);
    evaluator.relinearize_inplace(tmp_, relin_keys);
    evaluator.sub(tmp_res_, tmp_, tmp_res_);
    if (i == word_sz - 1) {
      res_[word_sz-1] = tmp_res_;
    } else {
      evaluator.multiply(res_[word_sz-1], tmp_res_, res_[word_sz-1]); // combine results
      evaluator.relinearize_inplace(res_[word_sz-1], relin_keys);
      encryptor.encrypt(zero, res_[i]); // Pad result with 0's
    }
  }
  return res_;
}

std::vector<seal::Ciphertext> neq_bin(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, seal::RelinKeys& relin_keys, 
    std::vector<seal::Ciphertext>& c1, std::vector<seal::Ciphertext>& c2, 
    size_t word_sz, size_t slots) {
    std::vector<seal::Ciphertext> res_ = eq_bin(evaluator, encryptor, batch_encoder,
      relin_keys, c1, c2, word_sz, slots);
    seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
    evaluator.negate(res_[word_sz-1], res_[word_sz-1]);
    evaluator.add_plain(res_[word_sz-1], one, res_[word_sz-1]);
    return res_;
}

std::vector<seal::Ciphertext> slice(
    std::vector<seal::Ciphertext>& in_, size_t start, size_t end) {
  std::vector<seal::Ciphertext> res_(end-start);
  for (size_t i = start; i < end; i++) {
    res_[i-start] = in_[i];
  }
  return res_;
}

// Vertical batching (assume all slots are independent)
std::vector<seal::Ciphertext> lt_bin(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, seal::RelinKeys& relin_keys, 
    std::vector<seal::Ciphertext>& c1, std::vector<seal::Ciphertext>& c2, 
    size_t word_sz, size_t slots) {
  std::vector<seal::Ciphertext> res_(word_sz);
  if (c1.size() == 1) {
    seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
    seal::Ciphertext c1neg;
    evaluator.negate(c1[0], c1neg);
    evaluator.add_plain(c1neg, one, c1neg);
    evaluator.multiply(c1neg, c2[0], res_[word_sz-1]);
    evaluator.relinearize_inplace(res_[word_sz-1], relin_keys);
    return res_;
  }
  int len = c1.size() >> 1;
  std::vector<seal::Ciphertext> lhs_h = slice(c1, 0, len);
  std::vector<seal::Ciphertext> lhs_l = slice(c1, len, c1.size());
  std::vector<seal::Ciphertext> rhs_h = slice(c2, 0, len);
  std::vector<seal::Ciphertext> rhs_l = slice(c2, len, c2.size());
  std::vector<seal::Ciphertext> term1 = lt_bin(evaluator, encryptor,
      batch_encoder, relin_keys, lhs_h, rhs_h, word_sz, slots);
  std::vector<seal::Ciphertext> h_equal = eq_bin(evaluator, encryptor,
      batch_encoder, relin_keys, lhs_h, rhs_h, lhs_h.size(), slots);
  std::vector<seal::Ciphertext> l_equal = lt_bin(evaluator, encryptor,
      batch_encoder, relin_keys, lhs_l, rhs_l, word_sz, slots);
  seal::Ciphertext term2;
  evaluator.multiply(h_equal[lhs_h.size()-1], l_equal[word_sz-1], term2);
  evaluator.relinearize_inplace(term2, relin_keys);
  res_[word_sz - 1] = exor(term1[word_sz-1], term2, evaluator, encryptor, relin_keys);
  seal::Plaintext zero = encode_all_slots(batch_encoder, 0, slots);
  for (size_t i = 0; i < word_sz - 1; i++) {
    encryptor.encrypt(zero, res_[i]); // Pad result with 0's
  }
  return res_;
}

// Vertical batching (assume all slots are independent)
std::vector<seal::Ciphertext> leq_bin(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, seal::RelinKeys& relin_keys, 
    std::vector<seal::Ciphertext>& c1, std::vector<seal::Ciphertext>& c2, 
    size_t word_sz, size_t slots) {
  std::vector<seal::Ciphertext> res_ = lt_bin(evaluator, encryptor, batch_encoder,
    relin_keys, c2, c1, word_sz, slots);
  evaluator.negate(res_[word_sz-1], res_[word_sz-1]);
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  evaluator.add_plain(res_[word_sz-1], one, res_[word_sz-1]);
  return res_;
}

/// Vertical batching (ripple carry)
std::vector<seal::Ciphertext> inc_bin(seal::Evaluator& evaluator, 
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder, 
    seal::RelinKeys& relin_keys, std::vector<seal::Ciphertext>& c1, 
    size_t slots) {
  seal::Plaintext carry_ptxt = encode_all_slots(batch_encoder, 1, slots);
  seal::Ciphertext carry_;
  encryptor.encrypt(carry_ptxt, carry_);
  std::vector<seal::Ciphertext> res_(c1.size());
  for (int i = c1.size()-1; i > 0; --i) {
    res_[i] = exor(c1[i], carry_, evaluator, encryptor, relin_keys);
    evaluator.multiply(c1[i], carry_, carry_);
    evaluator.relinearize_inplace(carry_, relin_keys);
  }
  res_[0] = exor(c1[0], carry_, evaluator, encryptor, relin_keys);
  return res_;
}

/// Vertical batching (ripple carry)
std::vector<seal::Ciphertext> dec_bin(seal::Evaluator& evaluator, 
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder, 
    seal::RelinKeys& relin_keys, std::vector<seal::Ciphertext>& c1, 
    size_t slots) {
  seal::Plaintext carry_ptxt = encode_all_slots(batch_encoder, 1, slots);
  seal::Ciphertext carry_, neg_c1;
  encryptor.encrypt(carry_ptxt, carry_);
  std::vector<seal::Ciphertext> res_(c1.size());
  for (int i = c1.size()-1; i > 0; --i) {
    res_[i] = exor(c1[i], carry_, evaluator, encryptor, relin_keys);
    evaluator.negate(c1[i], neg_c1);
    evaluator.add_plain(neg_c1, carry_ptxt, neg_c1);
    evaluator.multiply(neg_c1, carry_, carry_);
    evaluator.relinearize_inplace(carry_, relin_keys);
  }
  res_[0] = exor(c1[0], carry_, evaluator, encryptor, relin_keys);
  return res_;
}

/// Vertical batching (ripple carry)
std::vector<seal::Ciphertext> add_bin(seal::Evaluator& evaluator, 
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder, 
    seal::RelinKeys& relin_keys, std::vector<seal::Ciphertext>& c1, 
    std::vector<seal::Ciphertext>& c2, size_t slots) {
  seal::Plaintext carry_ptxt = encode_all_slots(batch_encoder, 0, slots);
  seal::Ciphertext carry_;
  encryptor.encrypt(carry_ptxt, carry_);
  std::vector<seal::Ciphertext>& smaller = (c1.size() < c2.size()) ? c1 : c2;
  std::vector<seal::Ciphertext>& bigger = (c1.size() < c2.size()) ? c2 : c1;
  size_t offset = bigger.size() - smaller.size();
  std::vector<seal::Ciphertext> res_(smaller.size());
  for (int i = smaller.size()-1; i >= 0; --i) {
    // sum = (c1 ^ c2) ^ in_carry
    seal::Ciphertext xor_ = exor(smaller[i], bigger[i + offset], evaluator, encryptor, relin_keys);
    res_[i] = exor(xor_, carry_, evaluator, encryptor, relin_keys);
    if (i == 0) break; // don't need output carry
    // next carry computation
    seal::Ciphertext prod_;
    evaluator.multiply(smaller[i], bigger[i + offset], prod_);
    evaluator.relinearize_inplace(prod_, relin_keys);
    evaluator.multiply(carry_, xor_, xor_);
    evaluator.relinearize_inplace(xor_, relin_keys);
    carry_ = exor(prod_, xor_, evaluator, encryptor, relin_keys);
  }
  return res_;
}

/// Vertical batching (assume inputs are the same size) (ripple carry)
std::vector<seal::Ciphertext> sub_bin(seal::Evaluator& evaluator, 
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder, 
    seal::RelinKeys& relin_keys, std::vector<seal::Ciphertext>& c1, 
    std::vector<seal::Ciphertext>& c2, size_t slots) {
  assert(c1.size() == c2.size());
  seal::Plaintext carry_ptxt = encode_all_slots(batch_encoder, 0, slots);
  seal::Ciphertext carry_;
  encryptor.encrypt(carry_ptxt, carry_);
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  std::vector<seal::Ciphertext> res_(c1.size());
  // Generate two's complement of c2
  std::vector<seal::Ciphertext> neg_c2(c2.size());
  for (int i = c2.size() - 1; i > -1; --i) {
    evaluator.negate(c2[i], neg_c2[i]);
    evaluator.add_plain(neg_c2[i], one, neg_c2[i]);
  }
  neg_c2 = inc_bin(evaluator, encryptor, batch_encoder, relin_keys, neg_c2, slots);
  for (int i = c1.size() - 1; i >= 0; --i) {
    // sum = (c1 ^ c2) ^ in_carry
    seal::Ciphertext xor_ = exor(c1[i], neg_c2[i], evaluator, encryptor, relin_keys);
    res_[i] = exor(xor_, carry_, evaluator, encryptor, relin_keys);
    if (i == 0) break; // don't need output carry
    // next carry computation
    seal::Ciphertext prod_;
    evaluator.multiply(c1[i], neg_c2[i], prod_);
    evaluator.relinearize_inplace(prod_, relin_keys);
    evaluator.multiply(carry_, xor_, xor_);
    evaluator.relinearize_inplace(xor_, relin_keys);
    carry_ = exor(prod_, xor_, evaluator, encryptor, relin_keys);
  }
  return res_;
}

/// Vertical batching (assume inputs are the same size)
std::vector<seal::Ciphertext> mult_bin(seal::Evaluator& evaluator, 
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder, 
    seal::RelinKeys& relin_keys, std::vector<seal::Ciphertext>& c1, 
    std::vector<seal::Ciphertext>& c2, size_t slots) {
  assert(c1.size() == c2.size());
  std::vector<seal::Ciphertext> tmp_(c1.size());
  std::vector<seal::Ciphertext> prod_(c1.size());
  seal::Plaintext zero = encode_all_slots(batch_encoder, 0, slots);
  for (int i = 0; i < prod_.size(); i++) {
    encryptor.encrypt(zero, prod_[i]);
  }
  for (int i = c1.size() - 1; i >= 0; i--) {
    for (int j = c2.size() - 1; j >= (int)c2.size() - 1 - i; --j) {
      evaluator.multiply(c1[i], c2[j], tmp_[j]);
      evaluator.relinearize_inplace(tmp_[j], relin_keys);
    }
    std::vector<seal::Ciphertext> tmp_slice_ = slice(prod_, 0, i + 1);
    tmp_slice_ = add_bin(evaluator, encryptor, batch_encoder, relin_keys, 
                         tmp_slice_, tmp_, slots);
    for (int j = i; j >= 0; --j) {
      prod_[j] = tmp_slice_[j];
    }
  }
  return prod_;
}

// Compute res = 1 - (x-y)^(ptxt_mod-1)
seal::Ciphertext eq(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext& c1, seal::Ciphertext& c2,
    size_t ptxt_mod) {
  seal::Ciphertext result_, tmp_, tmp2_;
  int num_squares = (int) log2(ptxt_mod - 1);
  tmp_ = c2;
  seal::Plaintext zero("0");
  encryptor.encrypt(zero, tmp_);
  evaluator.add(tmp_, c2, tmp_);
  evaluator.sub(c1, tmp_, tmp_);
  tmp2_ = tmp_;
  for (int i = 0; i < num_squares; i++) { // Square
    evaluator.square_inplace(tmp2_);
    evaluator.relinearize_inplace(tmp2_, relin_keys);
  }
  for (int i = 0; i < (ptxt_mod - 1 - pow(2, num_squares)); i++) { // Mult
    evaluator.multiply_inplace(tmp2_, tmp_);
    evaluator.relinearize_inplace(tmp2_, relin_keys);
  }
  seal::Plaintext one("1");
  encryptor.encrypt(one, result_);
  evaluator.sub(result_, tmp2_, result_);
  return result_;
}

seal::Ciphertext neq(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext& c1, seal::Ciphertext& c2,
    size_t ptxt_mod) {
  seal::Ciphertext res_ = eq(encryptor, evaluator, relin_keys, c1, c2, ptxt_mod);
  seal::Plaintext one("1");
  evaluator.negate(res_, res_);
  evaluator.add_plain(res_, one, res_);
  return res_;
}

seal::Ciphertext lt(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator, seal::RelinKeys& relin_keys,
    seal::Ciphertext& c1, seal::Ciphertext& c2, size_t ptxt_mod) {
  seal::Plaintext one("1");
  seal::Ciphertext one_;
  encryptor.encrypt(one, one_);
  seal::Ciphertext tmp_, tmp2_, result_;
  seal::Plaintext plain_zero("0");
  encryptor.encrypt(plain_zero, result_);
  int num_squares = (int) log2(ptxt_mod - 1);
  for (int i = -(ptxt_mod - 1)/2; i < 0; i++) {
    seal::Plaintext a = uint64_to_hex_string(i);
    encryptor.encrypt(plain_zero, tmp_);
    evaluator.add(tmp_, c2, tmp_);
    evaluator.sub(c1, tmp_, tmp_);
    evaluator.sub_plain(tmp_, a, tmp_);
    tmp2_ = tmp_;
    for (int j = 0; j < num_squares; j++) { // Square
      evaluator.multiply_inplace(tmp2_, tmp2_);
      evaluator.relinearize_inplace(tmp2_, relin_keys);
    }
    for (int j = 0; j < (ptxt_mod - 1 - pow(2, num_squares)); j++) { // Mult
      evaluator.multiply_inplace(tmp2_, tmp_);
      evaluator.relinearize_inplace(tmp2_, relin_keys);
    }
    evaluator.sub(one_, tmp2_, tmp_);
    evaluator.add_inplace(result_, tmp_);
  }
  return result_;
}

seal::Ciphertext leq(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator, seal::RelinKeys& relin_keys,
    seal::Ciphertext& c1, seal::Ciphertext& c2, size_t ptxt_mod) {
  seal::Ciphertext less_ = lt(encryptor, evaluator, relin_keys, c1, c2, ptxt_mod);
  seal::Ciphertext equal_ = eq(encryptor, evaluator, relin_keys, c1, c2, ptxt_mod);
  seal::Ciphertext tmp_, res_;
  evaluator.multiply(less_, equal_, tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  evaluator.add(less_, equal_, res_);
  evaluator.sub(res_, tmp_, res_);
  return res_;
}

seal::Ciphertext lt_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext& c1, seal::Plaintext& pt1,
    size_t ptxt_mod) {
  seal::Plaintext one("1");
  seal::Ciphertext one_;
  encryptor.encrypt(one, one_);
  seal::Ciphertext tmp_, tmp2_, result_;
  seal::Plaintext plain_zero("0");
  encryptor.encrypt(plain_zero, result_);
  int num_squares = (int) log2(ptxt_mod - 1);
  for (int i = -(ptxt_mod - 1)/2; i < 0; i++) {
    seal::Plaintext a = uint64_to_hex_string(i);
    evaluator.sub_plain(c1, pt1, tmp_);
    evaluator.sub_plain(tmp_, a, tmp_);
    tmp2_ = tmp_;
    for (int j = 0; j < num_squares; j++) { // Square
      evaluator.multiply_inplace(tmp2_, tmp2_);
      evaluator.relinearize_inplace(tmp2_, relin_keys);
    }
    for (int j = 0; j < (ptxt_mod - 1 - pow(2, num_squares)); j++) { // Mult
      evaluator.multiply_inplace(tmp2_, tmp_);
      evaluator.relinearize_inplace(tmp2_, relin_keys);
    }
    evaluator.sub(one_, tmp2_, tmp_);
    evaluator.add_inplace(result_, tmp_);
  }
  return result_;
}

seal::Ciphertext eq_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext& c1, seal::Plaintext& pt1,
    size_t ptxt_mod) {
  seal::Ciphertext result_, tmp_, tmp2_;
  int num_squares = (int) log2(ptxt_mod - 1);
  evaluator.sub_plain(c1, pt1, tmp_);
  tmp2_ = tmp_;
  for (int i = 0; i < num_squares; i++) { // Square
    evaluator.multiply_inplace(tmp2_, tmp2_);
    evaluator.relinearize_inplace(tmp2_, relin_keys);
  }
  for (int i = 0; i < (ptxt_mod - 1 - pow(2, num_squares)); i++) { // Mult
    evaluator.multiply_inplace(tmp2_, tmp_);
    evaluator.relinearize_inplace(tmp2_, relin_keys);
  }
  seal::Plaintext one("1");
  encryptor.encrypt(one, result_);
  evaluator.sub(result_, tmp2_, result_);
  return result_;
}

seal::Ciphertext neq_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext& c1, seal::Plaintext& pt1,
    size_t ptxt_mod) {
  seal::Ciphertext res_ = eq_plain(encryptor, evaluator, relin_keys, c1, pt1, 
    ptxt_mod);
  seal::Plaintext one("1");
  evaluator.negate(res_, res_);
  evaluator.add_plain(res_, one, res_);
  return res_;
}

seal::Ciphertext leq_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator, seal::RelinKeys& relin_keys,
    seal::Ciphertext& c1, seal::Plaintext& pt1, size_t ptxt_mod) {
  seal::Ciphertext less_ = lt_plain(encryptor, evaluator, relin_keys, c1, pt1, ptxt_mod);
  seal::Ciphertext equal_ = eq_plain(encryptor, evaluator, relin_keys, c1, pt1, ptxt_mod);
  seal::Ciphertext tmp_, res_;
  evaluator.multiply(less_, equal_, tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  evaluator.add(less_, equal_, res_);
  evaluator.sub(res_, tmp_, res_);
  return res_;
}
