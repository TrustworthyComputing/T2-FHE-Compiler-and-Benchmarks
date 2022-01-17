#include "functional_units.hpp"

seal::Ciphertext encrypt_num(seal::Encryptor& encryptor, uint64_t number) {
  seal::Ciphertext result;
  seal::Plaintext b(uint64_to_hex_string(number));
  encryptor.encrypt(b, result);
  return result;
}

uint64_t decrypt_num(seal::Decryptor& decryptor, seal::Ciphertext ctxt_) {
  seal::Plaintext result;
  decryptor.decrypt(ctxt_, result);
  return std::stol(result.to_string(), nullptr, 16);
}

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
    seal::BatchEncoder& batch_encoder,
    uint64_t num, size_t slots) {
  std::vector<uint64_t> vec_num(slots);
  for (int i = 0; i < vec_num.size(); ++i) {
    vec_num[i] = num;
  }
  seal::Plaintext vec_num_encoded;
  batch_encoder.encode(vec_num, vec_num_encoded);
  return vec_num_encoded;
}

seal::Ciphertext mult_all_slots(
    seal::Evaluator& evaluator, seal::Ciphertext ct1_, size_t slots,
    size_t padding, seal::GaloisKeys& galois_keys) {
  seal::Ciphertext res;
  res = ct1_;
  for (int i = 0; i < slots; i++) {
    evaluator.rotate_rows_inplace(res, padding, galois_keys);
    evaluator.multiply(res, ct1_, res);
  }
  return res;
}

std::vector<seal::Ciphertext> encrypt_num_to_binary_array(
    seal::Encryptor& encryptor, uint64_t number, size_t word_sz) {
  // Convert integer to binary.
  std::vector<seal::Ciphertext> result(word_sz);
  for (int i = 0; i < word_sz; ++i) {
    uint64_t val = (number >> i) & 1;
    // encode bit as integer
    seal::Plaintext b(uint64_to_hex_string(val));
    // encrypt bit
    encryptor.encrypt(b, result.at(i));
  }
  return result;
}

uint64_t decrypt_binary_array(
    seal::Decryptor& decryptor, std::vector<seal::Ciphertext>& encrypted_vec) {
  // Convert binary ciphertext to integer.
  uint64_t result = 0;
  seal::Plaintext tmp;
  for (int i = 0; i < encrypted_vec.size(); ++i) {
    decryptor.decrypt(encrypted_vec[i], tmp);

    uint64_t b = std::stol(tmp.to_string(), nullptr, 16);
    if (b == 1) {
      result |= ((uint64_t) 1 << i);
    }
  }
  return result;
}

seal::Ciphertext encrypt_num_to_binary_array_batch(
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder,
    uint64_t number, size_t word_sz, size_t slots, size_t padding /* = 1 */) {
  std::vector<uint64_t> ptxt_vec(slots);
  // Convert integer to binary.
  for (int i = 0; i < word_sz; ++i) {
    ptxt_vec[i * padding] = (number >> i) & 1;
  }
  seal::Plaintext ptxt_vec_encoded;
  batch_encoder.encode(ptxt_vec, ptxt_vec_encoded);
  seal::Ciphertext encrypted_vec;
  encryptor.encrypt(ptxt_vec_encoded, encrypted_vec);
  return encrypted_vec;
}

uint64_t decrypt_binary_array_batch(
    seal::Decryptor& decryptor, seal::BatchEncoder& batch_encoder,
    seal::Ciphertext encrypted_vec, size_t word_sz, size_t padding /* = 1 */) {
  seal::Plaintext ptxt_vec_encoded;
  decryptor.decrypt(encrypted_vec, ptxt_vec_encoded);
  std::vector<uint64_t> ptxt_vec;
  batch_encoder.decode(ptxt_vec_encoded, ptxt_vec);
  uint64_t result = 0;
  for (int i = 0; i < word_sz; ++i) {
    if (ptxt_vec[i * padding] == 1) {
      result |= ((uint64_t) 1 << i);
    }
  }
  return result;
}

seal::Ciphertext encrypt_nums_to_array_batch(seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, std::vector<uint64_t> nums,
    size_t num_elems, size_t slots, size_t padding /* = 1 */) {
  std::vector<uint64_t> ptxt_vec(slots);
  for (int i = 0; i < num_elems; ++i) {
    ptxt_vec[i * padding] = nums[i];
  }
  seal::Plaintext ptxt_vec_encoded;
  batch_encoder.encode(ptxt_vec, ptxt_vec_encoded);
  seal::Ciphertext encrypted_vec;
  encryptor.encrypt(ptxt_vec_encoded, encrypted_vec);
  return encrypted_vec;
}

std::vector<uint64_t> decrypt_array_batch_to_nums(
    seal::Decryptor& decryptor, seal::BatchEncoder& batch_encoder,
    seal::Ciphertext encrypted_vec, size_t slots, size_t padding /* = 1 */) {
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

seal::Ciphertext xor_batch(seal::Ciphertext& ctxt_1, seal::Ciphertext& ctxt_2,
                           seal::Evaluator& evaluator,
                           const seal::RelinKeys& relinKeys) {
  // https://stackoverflow.com/a/46674398
  seal::Ciphertext result;
  evaluator.sub(ctxt_1, ctxt_2, result);
  evaluator.square_inplace(result);
  evaluator.relinearize_inplace(result, relinKeys);
  return result;
}

// True batching, assume all slots are independent (XNOR)
seal::Ciphertext eq_bin_batched(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext ct1_, seal::Ciphertext ct2_,
    seal::GaloisKeys& galios_keys, size_t slots, size_t padding) {
  seal::Ciphertext res_, tmp_;
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  encryptor.encrypt(one, res_);
  evaluator.sub(ct1_, ct2_, tmp_);
  evaluator.square_inplace(tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  evaluator.sub(res_, tmp_, res_);
  return res_;
}

// Vertical batching (assume all slots are independent) (XNOR)
std::vector<seal::Ciphertext> eq_bin(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, seal::RelinKeys& relin_keys, 
    std::vector<seal::Ciphertext>& ct1_, std::vector<seal::Ciphertext>& ct2_, 
    size_t word_sz, size_t slots) {
  std::vector<seal::Ciphertext> res_(word_sz);
  seal::Ciphertext tmp_, tmp_res_;
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  seal::Plaintext zero = encode_all_slots(batch_encoder, 0, slots);
  for (int i = word_sz-1; i > -1; i--) {
    encryptor.encrypt(one, tmp_res_);
    evaluator.sub(ct1_[i], ct2_[i], tmp_);
    evaluator.square_inplace(tmp_);
    evaluator.relinearize_inplace(tmp_, relin_keys);
    evaluator.sub(tmp_res_, tmp_, tmp_res_);
    if (i == (word_sz-1)) {
      res_[word_sz-1] = tmp_res_;
    }
    else {
      evaluator.multiply(res_[word_sz-1], tmp_res_, res_[word_sz-1]); // combine results
      evaluator.relinearize_inplace(res_[word_sz-1], relin_keys);
      encryptor.encrypt(zero, res_[i]); // Pad result with 0's
    }
  }
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
    std::vector<seal::Ciphertext>& ct1_, std::vector<seal::Ciphertext>& ct2_, 
    size_t word_sz, size_t slots) {
  std::vector<seal::Ciphertext> res_(word_sz);
  if (ct1_.size() == 1) {
    seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
    seal::Ciphertext ct1_neg;
    evaluator.negate(ct1_[0], ct1_neg);
    evaluator.add_plain(ct1_neg, one, ct1_neg);
    evaluator.multiply(ct1_neg, ct2_[0], res_[word_sz-1]);
    evaluator.relinearize_inplace(res_[word_sz-1], relin_keys);
    return res_;
  }

  int len = ct1_.size() >> 1;

  std::vector<seal::Ciphertext> lhs_h = slice(ct1_, 0, len);
  std::vector<seal::Ciphertext> lhs_l = slice(ct1_, len, ct1_.size());
  std::vector<seal::Ciphertext> rhs_h = slice(ct2_, 0, len);
  std::vector<seal::Ciphertext> rhs_l = slice(ct2_, len, ct2_.size());

  std::vector<seal::Ciphertext> term1 = lt_bin(evaluator, encryptor, batch_encoder,
    relin_keys, lhs_h, rhs_h, word_sz, slots);
  std::vector<seal::Ciphertext> h_equal = eq_bin(evaluator, encryptor, batch_encoder,
    relin_keys, lhs_h, rhs_h, lhs_h.size(), slots);
  std::vector<seal::Ciphertext> l_equal = lt_bin(evaluator, encryptor, batch_encoder,
    relin_keys, lhs_l, rhs_l, word_sz, slots);

  seal::Ciphertext term2;

  evaluator.multiply(h_equal[lhs_h.size()-1], l_equal[word_sz-1], term2);
  evaluator.relinearize_inplace(term2, relin_keys);

  res_[word_sz-1] = xor_batch(term1[word_sz-1], term2, evaluator, relin_keys);
  seal::Plaintext zero = encode_all_slots(batch_encoder, 0, slots);
  for (size_t i = 0; i < (word_sz-1); i++) {
    encryptor.encrypt(zero, res_[i]); // Pad result with 0's
  }
  return res_;
}


// True batching, assume all slots are independent
seal::Ciphertext lt_bin_batched(
    seal::Evaluator& evaluator, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext ct1_, seal::Ciphertext ct2_,
    size_t slots) {
  seal::Ciphertext res_;
  seal::Ciphertext tmp_;
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  evaluator.sub_plain(ct1_, one, tmp_);
  evaluator.square_inplace(tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);

  evaluator.multiply(tmp_, ct2_, res_);
  evaluator.relinearize_inplace(res_, relin_keys);
  return res_;
}

seal::Ciphertext lt_bin_batched_plain(
    seal::Evaluator& evaluator, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext ct1_, seal::Plaintext pt1_,
    size_t slots) {
  seal::Ciphertext res_;
  seal::Ciphertext tmp_;
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  evaluator.sub_plain(ct1_, one, tmp_);
  evaluator.square_inplace(tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);

  evaluator.multiply_plain(tmp_, pt1_, res_);
  return res_;
}

seal::Ciphertext lte_bin_batched_plain(
    seal::Evaluator& evaluator, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext ct1_, seal::Plaintext pt1_,
    size_t slots) {
  seal::Ciphertext res_, tmp_, less_, equal_;

  // Less-
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  evaluator.sub_plain(ct1_, one, tmp_);
  evaluator.square_inplace(tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  evaluator.multiply_plain(tmp_, pt1_, less_);

  // Equal
  evaluator.multiply_plain(ct1_, pt1_, equal_);

  // Less-than OR Equal
  evaluator.multiply(less_, equal_, tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  evaluator.add(less_, equal_, res_);
  evaluator.sub(res_, tmp_, res_);
  return res_;
}

seal::Ciphertext lte_bin_batched(
    seal::Evaluator& evaluator, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext ct1_, seal::Plaintext pt1_,
    size_t slots) {
  seal::Ciphertext res_, tmp_, less_, equal_;

  // Less-than
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  evaluator.sub_plain(ct1_, one, tmp_);
  evaluator.square_inplace(tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  evaluator.multiply_plain(tmp_, pt1_, less_);

  // Equal
  evaluator.multiply_plain(ct1_, pt1_, equal_);

  // Less-than OR Equal
  evaluator.multiply(less_, equal_, tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  evaluator.add(less_, equal_, res_);
  evaluator.sub(res_, tmp_, res_);
  return res_;
}

seal::Ciphertext eq_bin_batched_plain(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext ct1_, seal::Plaintext pt1,
    seal::GaloisKeys& galios_keys, size_t slots, size_t padding) {
  seal::Ciphertext res_, tmp_;
  seal::Plaintext one = encode_all_slots(batch_encoder, 1, slots);
  encryptor.encrypt(one, res_);
  evaluator.sub_plain(ct1_, pt1, tmp_);
  evaluator.square_inplace(tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  evaluator.sub(res_, tmp_, res_);
  // res = mult_all_slots(evaluator, res, slots, padding, galois_keys);
  return res_;
}
seal::Ciphertext eq_bin_batched_plain(
    seal::Evaluator& evaluator, seal::BatchEncoder& batch_encoder,
    seal::Ciphertext ct1_, seal::Plaintext pt1_, seal::GaloisKeys& galios_keys,
    size_t slots, size_t padding) {
  seal::Ciphertext res;
  evaluator.multiply_plain(ct1_, pt1_, res);
  // res = mult_all_slots(evaluator, res, slots, padding, galois_keys);
  return res;
}

// Compute res = 1 - (x-y)^(ptxt_mod-1)
seal::Ciphertext eq(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext ct1_, seal::Ciphertext ct2_,
    size_t ptxt_mod) {
  seal::Ciphertext result_, tmp_, tmp2_;
  int num_squares = (int)log2(ptxt_mod-1);
  evaluator.sub(ct1_, ct2_, tmp_);
  tmp2_ = tmp_;
  for (int i = 0; i < num_squares; i++) { // Square
    evaluator.square_inplace(tmp2_);
    evaluator.relinearize_inplace(tmp2_, relin_keys);
  }
  for (int i = 0; i < ((ptxt_mod-1) - pow(2,num_squares)); i++) { // Mult
    evaluator.multiply_inplace(tmp2_, tmp_);
    evaluator.relinearize_inplace(tmp2_, relin_keys);
  }
  seal::Plaintext one("1");
  encryptor.encrypt(one, result_);
  evaluator.sub(result_, tmp2_, result_);
  return result_;
}

seal::Ciphertext lt(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator, seal::RelinKeys& relin_keys,
    seal::Ciphertext ct1_, seal::Ciphertext ct2_, size_t ptxt_mod) {
  seal::Plaintext one("1");
  seal::Ciphertext one_;
  encryptor.encrypt(one, one_);
  seal::Ciphertext tmp_, tmp2_, result_;
  seal::Plaintext plain_zero("0");
  encryptor.encrypt(plain_zero, result_);

  int num_squares = (int)log2(ptxt_mod-1);

  for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
    seal::Plaintext a = uint64_to_hex_string(i);

    evaluator.sub(ct1_, ct2_, tmp_);
    evaluator.sub_plain(tmp_, a, tmp_);
    tmp2_ = tmp_;
    for (int j = 0; j < num_squares; j++) { // Square
      evaluator.multiply_inplace(tmp2_, tmp2_);
      evaluator.relinearize_inplace(tmp2_, relin_keys);
    }
    for (int j = 0; j < ((ptxt_mod-1) - pow(2,num_squares)); j++) { // Mult
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
    seal::Ciphertext ct1_, seal::Ciphertext ct2_, size_t ptxt_mod) {
  seal::Ciphertext less_ = lt(encryptor, evaluator, relin_keys, ct1_, ct2_, ptxt_mod);
  seal::Ciphertext equal_ = eq(encryptor, evaluator, relin_keys, ct1_, ct2_, ptxt_mod);
  seal::Ciphertext tmp_, res_;

  evaluator.multiply(less_, equal_, tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  evaluator.add(less_, equal_, res_);
  evaluator.sub(res_, tmp_, res_);

  return res_;
}

seal::Ciphertext lt_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext ct1_, seal::Plaintext pt1_,
    size_t ptxt_mod) {
  seal::Plaintext one("1");
  seal::Ciphertext one_;
  encryptor.encrypt(one, one_);

  seal::Ciphertext tmp_, tmp2_, result_;
  seal::Plaintext plain_zero("0");
  encryptor.encrypt(plain_zero, result_);

  int num_squares = (int)log2(ptxt_mod-1);

  for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
    seal::Plaintext a = uint64_to_hex_string(i);
    evaluator.sub_plain(ct1_, pt1_, tmp_);
    evaluator.sub_plain(tmp_, a, tmp_);
    tmp2_ = tmp_;
    for (int j = 0; j < num_squares; j++) { // Square
      evaluator.multiply_inplace(tmp2_, tmp2_);
      evaluator.relinearize_inplace(tmp2_, relin_keys);
    }
    for (int j = 0; j < ((ptxt_mod-1) - pow(2,num_squares)); j++) { // Mult
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
    seal::RelinKeys& relin_keys, seal::Ciphertext ct1_, seal::Plaintext pt1_,
    size_t ptxt_mod) {
  seal::Ciphertext result_, tmp_, tmp2_;
  int num_squares = (int)log2(ptxt_mod-1);
  evaluator.sub_plain(ct1_, pt1_, tmp_);
  tmp2_ = tmp_;
  for (int i = 0; i < num_squares; i++) { // Square
    evaluator.multiply_inplace(tmp2_, tmp2_);
    evaluator.relinearize_inplace(tmp2_, relin_keys);
  }
  for (int i = 0; i < ((ptxt_mod-1) - pow(2,num_squares)); i++) { // Mult
    evaluator.multiply_inplace(tmp2_, tmp_);
    evaluator.relinearize_inplace(tmp2_, relin_keys);
  }
  seal::Plaintext one("1");
  encryptor.encrypt(one, result_);
  evaluator.sub(result_, tmp2_, result_);
  return result_;
}

seal::Ciphertext leq_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator, seal::RelinKeys& relin_keys,
    seal::Ciphertext ct1_, seal::Plaintext pt1_, size_t ptxt_mod) {
  seal::Ciphertext less_ = lt_plain(encryptor, evaluator, relin_keys, ct1_, pt1_, ptxt_mod);
  seal::Ciphertext equal_ = eq_plain(encryptor, evaluator, relin_keys, ct1_, pt1_, ptxt_mod);
  seal::Ciphertext tmp_, res_;

  evaluator.multiply(less_, equal_, tmp_);
  evaluator.relinearize_inplace(tmp_, relin_keys);
  evaluator.add(less_, equal_, res_);
  evaluator.sub(res_, tmp_, res_);

  return res_;
}
