#include "functional_units.hpp"

std::vector<helib::Ctxt> encrypt_num_to_binary_array(
    helib::PubKey& public_key, helib::Context& context, uint64_t number, 
    size_t word_sz) {
  // Convert integer to binary.
  std::vector<helib::Ctxt> result;
  for (int i = 0; i < word_sz; ++i) {
    helib::Ctxt ct(public_key);
    uint64_t val = (number >> i) & 1;
    // encode bit as integer
    helib::Ptxt<helib::BGV> b(context);
    b[0] = val;
    // encrypt bit
    public_key.Encrypt(ct, b);
    result.push_back(ct);
  }
  return result;
}

uint64_t decrypt_binary_array(
    helib::SecKey& secret_key, helib::Context& context, 
    std::vector<helib::Ctxt>& encrypted_vec) {
  // Convert binary ciphertext to integer.
  uint64_t result = 0;
  helib::Ptxt<helib::BGV> tmp(context);
  for (int i = 0; i < encrypted_vec.size(); ++i) {
    secret_key.Decrypt(tmp, encrypted_vec[i]);
    uint64_t b = (uint64_t)IsOne(coeff(tmp.getSlotRepr()[0].getData(), 0));
    if (b == 1) {
      result |= ((uint64_t) 1 << i);
    }
  }
  return result;
}

helib::Ctxt encrypt_num_to_binary_array_batch(
    helib::PubKey& public_key, helib::Context& context, uint64_t number, 
    size_t word_sz, size_t slots, size_t padding /* = 1 */) {
  helib::Ptxt<helib::BGV> ptxt_vec(context);
  // Convert integer to binary.
  for (int i = 0; i < word_sz; ++i) {
    ptxt_vec[i * padding] = (number >> i) & 1;
  }
  helib::Ctxt encrypted_vec(public_key);
  public_key.Encrypt(encrypted_vec, ptxt_vec);
  return encrypted_vec;
}

uint64_t decrypt_binary_array_batch(
    helib::SecKey& secret_key, helib::Context& context, 
    helib::Ctxt encrypted_vec, size_t word_sz, size_t padding /* = 1 */) {
  helib::Ptxt<helib::BGV> ptxt_vec_encoded(context);
  secret_key.Decrypt(ptxt_vec_encoded, encrypted_vec);
  uint64_t result = 0;
  for (int i = 0; i < word_sz; ++i) {
    if (IsOne(coeff(ptxt_vec_encoded.getSlotRepr()[i].getData(), 0))) {
      result |= ((uint64_t) 1 << i);
    }
  }
  return result;
}

helib::Ctxt encrypt_nums_to_array_batch(helib::PubKey& public_key, 
    helib::Context& context, std::vector<uint64_t> nums,  size_t num_elems, 
    size_t slots, size_t padding /* = 1 */) {
  helib::Ptxt<helib::BGV> ptxt_vec(context);
  for (int i = 0; i < num_elems; ++i) {
    ptxt_vec[i * padding] = nums[i];
  }
  helib::Ctxt encrypted_vec(public_key);
  public_key.Encrypt(encrypted_vec, ptxt_vec);
  return encrypted_vec; 
}

std::vector<uint64_t> decrypt_array_batch_to_nums(helib::SecKey& secret_key, 
    helib::Context& context, helib::Ctxt encrypted_vec, size_t slots, 
    size_t padding /* = 1 */) {
  helib::Ptxt<helib::BGV> ptxt_vec_encoded(context);
  secret_key.Decrypt(ptxt_vec_encoded, encrypted_vec);
  std::vector<uint64_t> result_vec(slots / padding);
  for (int i = 0; i < slots/padding; ++i) {
    conv(result_vec[i], coeff(ptxt_vec_encoded.getSlotRepr()[i * padding].getData(), 0));
  }
  return result_vec;
}


