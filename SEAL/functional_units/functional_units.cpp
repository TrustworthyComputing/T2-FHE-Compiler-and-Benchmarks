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
  return std::stol(ptxt.to_string(), nullptr, 16);
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

seal::Ciphertext encrypt_num_vec_batch(
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder,
    std::vector<uint64_t> nums, size_t slots, size_t padding /* = 1 */) {
  std::vector<uint64_t> ptxt_vec(slots);
  for (int i = 0; i < nums.size(); ++i) {
    ptxt_vec[i * padding] = nums[i];
  }
  seal::Plaintext ptxt_vec_encoded;
  batch_encoder.encode(ptxt_vec, ptxt_vec_encoded);
  seal::Ciphertext encrypted_vec;
  encryptor.encrypt(ptxt_vec_encoded, encrypted_vec);
  return encrypted_vec;
}

std::vector<uint64_t> decrypt_num_vec_batch(
    seal::Decryptor& decryptor, seal::BatchEncoder& batch_encoder,
    seal::Ciphertext encrypted_vec, size_t padding /* = 1 */) {
  seal::Plaintext ptxt_vec_encoded;
  decryptor.decrypt(encrypted_vec, ptxt_vec_encoded);
  std::vector<uint64_t> ptxt_vec;
  batch_encoder.decode(ptxt_vec_encoded, ptxt_vec);
  return result;
}

// Compute res = 1 - (x-y)^(ptxt_mod-1)
seal::Ciphertext eq(
    seal::Evaluator& evaluator, seal::Ciphertext ct1_, seal::Ciphertext ct2_,
    size_t ptxt_mod) {
  seal::Ciphertext result_, tmp_, tmp2_;
  int num_squares = (int)log2(ptxt_mod-1);
  evaluator.sub(ct1_, ct2_, tmp_);
  tmp2_ = tmp_;
  for (int i = 0; i < num_squares; i++) { // Square
    evaluator.mult_inplace(tmp2_, tmp2_);
  }
  for (int i = 0; i < ((ptxt_mod-1) - pow(2,num_squares)); i++) { // Mult
    evaluator.mult_inplace(tmp2_, tmp_);
  }
}

seal::Ciphertext lt(
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder,
    seal::Evaluator& evaluator, seal::Ciphertext ct1_, seal::Ciphertext ct2_,
    size_t ptxt_mod, size_t slots) {
    seal::Ciphertext one_ = encrypt_all_slots(encryptor, batch_encoder, 1, slots);
    seal::Ciphertext tmp_, tmp2_, result_;
    seal::Plaintext plain_zero("0");
    encryptor.encrypt(plain_zero, result_);

    int num_squares = (int)log2(ptxt_mod-1);

    for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
      seal::Plaintext a = encode_all_slots(batch_encoder, i, slots);
      evaluator.sub(ct1_, ct2_, tmp_);
      evaluator.sub_plain(tmp_, a, tmp_);
      tmp2_ = tmp_;
      for (int j = 0; j < num_squares; j++) { // Square
        evaluator.mult_inplace(tmp2_, tmp2_);
      }
      for (int j = 0; j < ((ptxt_mod-1) - pow(2,num_squares)); j++) { // Mult
        evaluator.mult_inplace(tmp2_, tmp_);
      }
      evaluator.sub(one, tmp2_, tmp_);
      evaluator.add_inplace(result_, tmp_);
    }
    return result_;
}

seal::Ciphertext lt_plain(
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder,
    seal::Evaluator& evaluator, seal::Ciphertext ct1_, seal::Plaintext pt1_,
    size_t ptxt_mod, size_t slots) {
    seal::Ciphertext one_ = encrypt_all_slots(encryptor, batch_encoder, 1, slots);
    seal::Ciphertext tmp_, tmp2_, result_;
    seal::Plaintext plain_zero("0");
    encryptor.encrypt(plain_zero, result_);

    int num_squares = (int)log2(ptxt_mod-1);

    for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
      seal::Plaintext a = encode_all_slots(batch_encoder, i, slots);
      evaluator.sub_plain(ct1_, pt1_, tmp_);
      evaluator.sub_plain(tmp_, a, tmp_);
      tmp2_ = tmp_;
      for (int j = 0; j < num_squares; j++) { // Square
        evaluator.mult_inplace(tmp2_, tmp2_);
      }
      for (int j = 0; j < ((ptxt_mod-1) - pow(2,num_squares)); j++) { // Mult
        evaluator.mult_inplace(tmp2_, tmp_);
      }
      evaluator.sub(one, tmp2_, tmp_);
      evaluator.add_inplace(result_, tmp_);
    }
    return result_;
}

seal::Ciphertext eq_plain(
    seal::Evaluator& evaluator, seal::Ciphertext ct1_, seal::Plaintext pt1_,
    size_t ptxt_mod) {
  seal::Ciphertext result_, tmp_, tmp2_;
  int num_squares = (int)log2(ptxt_mod-1);
  evaluator.sub_plain(ct1_, pt1_, tmp_);
  tmp2_ = tmp_;
  for (int i = 0; i < num_squares; i++) { // Square
    evaluator.mult_inplace(tmp2_, tmp2_);
  }
  for (int i = 0; i < ((ptxt_mod-1) - pow(2,num_squares)); i++) { // Mult
    evaluator.mult_inplace(tmp2_, tmp_);
  }
}
