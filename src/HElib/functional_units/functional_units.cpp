#include "functional_units.hpp"

std::vector<helib::Ctxt> ctptr_to_vec(helib::PubKey& public_key, 
                                      helib::CtPtrs_vectorCt& ct_) {
  helib::Ctxt scratch(public_key);
  size_t ct_size = helib::lsize(ct_);
  std::vector<helib::Ctxt> res_(ct_size, scratch);
  for (size_t i = 0; i < ct_size; ++i) {
    res_[i] = *ct_[i];
  }
  return res_;
}

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
    helib::Ctxt& encrypted_vec, size_t word_sz, size_t padding /* = 1 */) {
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
    helib::Context& context, std::vector<uint64_t>& nums, size_t num_elems, 
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
    helib::Context& context, helib::Ctxt& encrypted_vec, size_t slots, 
    size_t padding /* = 1 */) {
  helib::Ptxt<helib::BGV> ptxt_vec_encoded(context);
  secret_key.Decrypt(ptxt_vec_encoded, encrypted_vec);
  std::vector<uint64_t> result_vec(slots / padding);
  for (int i = 0; i < slots/padding; ++i) {
    conv(result_vec[i], coeff(ptxt_vec_encoded.getSlotRepr()[i * padding].getData(), 0));
  }
  return result_vec;
}

helib::Ctxt exor(helib::PubKey& public_key, helib::Ctxt& ctxt_1, 
    helib::Ctxt& ctxt_2) {
  helib::Ctxt result(public_key);
  result = ctxt_1;
  result -= ctxt_2;
  result.multiplyBy(result);
  return result;
}

helib::Ctxt eq_bin_batched(helib::PubKey& public_key, helib::Context& context, 
    helib::Ctxt& ct1_, helib::Ctxt& ct2_, size_t slots, const helib::EncryptedArray& ea,
    size_t padding) {
  helib::Ctxt res_(public_key);
  helib::Ctxt tmp_(public_key);
  helib::Ptxt<helib::BGV> ptxt_ones(context);
  for (int i = 0; i < ptxt_ones.size(); i++) {
    ptxt_ones[i] = 1;
  }
  public_key.Encrypt(res_, ptxt_ones);
  tmp_ = ct1_;
  tmp_ -= ct2_;
  tmp_.multiplyBy(tmp_);
  res_ -= tmp_;
  return res_;
}

helib::Ctxt lt_bin_batched(helib::PubKey& public_key, helib::Ctxt& ct1_, 
    helib::Ctxt& ct2_, size_t slots) {
  helib::Ctxt res_(public_key);
  helib::Ctxt tmp_(public_key);
  tmp_ = ct1_;
  tmp_.addConstant(NTL::ZZX(-1));
  tmp_.multiplyBy(tmp_);
  res_ = ct2_;
  res_.multiplyBy(tmp_);
  return res_;
}

helib::Ctxt lt_bin_batched_plain(helib::PubKey& public_key, helib::Ctxt& ct1_, 
    helib::Ptxt<helib::BGV>& pt1_, size_t slots) {
  helib::Ctxt res_(public_key);
  res_ = ct1_;
  res_.addConstant(NTL::ZZX(-1));
  res_.multiplyBy(res_);
  res_.multByConstant(pt1_);
  return res_;
}

helib::Ctxt lte_bin_batched_plain(helib::PubKey& public_key, helib::Ctxt& ct1_, 
    helib::Ptxt<helib::BGV>& pt1_, size_t slots) {
  helib::Ctxt res_(public_key);
  helib::Ctxt tmp_(public_key);
  helib::Ctxt less_(public_key);
  helib::Ctxt equal_(public_key);

  // Less
  less_ = ct1_;
  less_.addConstant(NTL::ZZX(-1));
  less_.multiplyBy(less_);
  less_.multByConstant(pt1_);

  // Equal
  equal_ = ct1_;
  equal_.multByConstant(pt1_);

  // Less-than OR Equal
  tmp_ = less_;
  tmp_.multiplyBy(equal_);
  res_ = less_;
  res_ += equal_;
  res_ -= tmp_;

  return res_;
}

helib::Ctxt lte_bin_batched(helib::PubKey& public_key, helib::Ctxt& ct1_, 
    helib::Ctxt& ct2_, size_t slots) {
  helib::Ctxt res_(public_key);
  helib::Ctxt tmp_(public_key);
  helib::Ctxt less_(public_key);
  helib::Ctxt equal_(public_key);

  // Less-than
  less_ = ct1_;
  less_.addConstant(NTL::ZZX(-1));
  less_.multiplyBy(less_);
  less_.multiplyBy(ct2_);

  // Equal
  equal_ = ct1_;
  equal_.multiplyBy(ct2_);

  // Less-than OR Equal
  tmp_ = less_;
  tmp_.multiplyBy(equal_);
  res_ = less_;
  res_ += equal_;
  res_ -= tmp_;
  return res_;
}

helib::Ctxt eq_bin_batched_plain(helib::PubKey& public_key, 
    helib::Context& context, helib::Ctxt& ct1_, helib::Ptxt<helib::BGV>& pt1_, 
    helib::EncryptedArray& ea, size_t slots, size_t padding) {
  helib::Ctxt res_(public_key);
  helib::Ctxt tmp_(public_key);
  helib::Ptxt<helib::BGV> ptxt_ones(context);
  for (int i = 0; i < ptxt_ones.size(); i++) {
    ptxt_ones[i] = 1;
  }
  public_key.Encrypt(res_, ptxt_ones);
  tmp_ = ct1_;
  pt1_.negate();
  tmp_.addConstant(pt1_);
  pt1_.negate();
  tmp_.multiplyBy(tmp_);
  res_ -= tmp_;
  return res_;
}

std::vector<helib::Ctxt> add_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& ct1_, 
                                 std::vector<helib::Ctxt>& ct2_, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  std::vector<helib::Ctxt> res_;
  helib::CtPtrs_vectorCt output_wrapper(res_);
  helib::addTwoNumbers(
      output_wrapper,
      helib::CtPtrs_vectorCt(ct1_),
      helib::CtPtrs_vectorCt(ct2_),
      ct1_.size(), &unpackSlotEncoding);
  
  return ctptr_to_vec(public_key, output_wrapper);
}

std::vector<helib::Ctxt> xor_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& ct1_, 
                                 std::vector<helib::Ctxt>& ct2_, 
                                 size_t ptxt_mod) {
  assert(ct1_.size() == ct2_.size());
  helib::Ctxt scratch(public_key);
  std::vector<helib::Ctxt> res_(ct1_.size(), scratch);
  if (ptxt_mod > 2) {
    for (int i = 0; i < res_.size(); i++) {
      res_[i] = ct1_[i];
      res_[i] -= ct2_[i];
      res_[i] *= res_[i];
    }
  } else {
    for (int i = 0; i < res_.size(); i++) {
      res_[i] = ct1_[i];
      res_[i] += ct2_[i];
    }
  }
  return res_;
}


std::vector<helib::Ctxt> sub_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& ct1_, 
                                 std::vector<helib::Ctxt>& ct2_, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  helib::Ctxt scratch(public_key);
  std::vector<helib::Ctxt> res_(ct1_.size(), scratch);
  helib::CtPtrs_vectorCt diff(res_);
  helib::subtractBinary(
      diff,
      helib::CtPtrs_vectorCt(ct1_),
      helib::CtPtrs_vectorCt(ct2_),
      &unpackSlotEncoding);
  
  return ctptr_to_vec(public_key, diff);
}

std::vector<helib::Ctxt> mult_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& ct1_, 
                                 std::vector<helib::Ctxt>& ct2_, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  std::vector<helib::Ctxt> res_;
  helib::CtPtrs_vectorCt prod(res_);
  helib::multTwoNumbers(
      prod,
      helib::CtPtrs_vectorCt(ct1_),
      helib::CtPtrs_vectorCt(ct2_),
      false, ct1_.size(), &unpackSlotEncoding);
  
  return ctptr_to_vec(public_key, prod);
}

std::vector<helib::Ctxt> eq_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& ct1_, 
                                 std::vector<helib::Ctxt>& ct2_, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  helib::Ctxt mu_(public_key), ni_(public_key);
  helib::compareTwoNumbers(mu_, ni_, helib::CtPtrs_vectorCt(ct1_), 
                           helib::CtPtrs_vectorCt(ct2_), false, 
                           &unpackSlotEncoding);
  mu_.negate();
  mu_.addConstant(NTL::ZZX(1));
  ni_.negate();
  ni_.addConstant(NTL::ZZX(1));
  mu_.multiplyBy(ni_);

  std::vector<helib::Ctxt> res_(ct1_.size(), mu_);
  for (size_t i = 1; i < res_.size(); ++i) {
    res_[i].clear();
  }
  return res_;
}

std::vector<helib::Ctxt> lt_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& ct1_, 
                                 std::vector<helib::Ctxt>& ct2_, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  helib::Ctxt mu_(public_key), ni_(public_key);
  helib::compareTwoNumbers(mu_, ni_, helib::CtPtrs_vectorCt(ct1_), 
                           helib::CtPtrs_vectorCt(ct2_), false, 
                           &unpackSlotEncoding);

  std::vector<helib::Ctxt> res_(ct1_.size(), ni_);
  for (size_t i = 1; i < res_.size(); ++i) {
    res_[i].clear();
  }
  return res_;
}

std::vector<helib::Ctxt> leq_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& ct1_, 
                                 std::vector<helib::Ctxt>& ct2_, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  helib::Ctxt mu_(public_key), ni_(public_key);
  helib::compareTwoNumbers(mu_, ni_, helib::CtPtrs_vectorCt(ct1_), 
                           helib::CtPtrs_vectorCt(ct2_), false, 
                           &unpackSlotEncoding);

  mu_.negate();
  mu_.addConstant(NTL::ZZX(1));
  std::vector<helib::Ctxt> res_(ct1_.size(), mu_);
  for (size_t i = 1; i < res_.size(); ++i) {
    res_[i].clear();
  }
  return res_;
}

helib::Ctxt eq(
    helib::PubKey& public_key, helib::Ctxt& ct1_, helib::Ctxt& ct2_, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt result_(public_key), tmp_(public_key), tmp2_(public_key);
  int num_squares = (int)log2(ptxt_mod-1);
  tmp_ = ct1_;
  tmp_ -= ct2_;
  tmp2_ = tmp_;
  for (int i = 0; i < num_squares; i++) { // Square
    tmp2_.multiplyBy(tmp2_);
  }
  for (int i = 0; i < ((ptxt_mod-1) - pow(2,num_squares)); i++) { // Mult
    tmp2_ *= tmp_;
  }
  result_ = tmp2_;
  result_.negate();
  result_.addConstant(NTL::ZZX(1));
  return result_;
}

helib::Ctxt lt(
    helib::PubKey& public_key, helib::Ctxt& ct1_, helib::Ctxt& ct2_, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt tmp_(public_key), tmp2_(public_key), result_(public_key);
  result_.clear();

  int num_squares = (int)log2(ptxt_mod-1);

  for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
    tmp_ = ct1_;
    tmp_ -= ct2_;
    tmp_.addConstant(NTL::ZZX(-i));
    tmp2_ = tmp_;
    for (int j = 0; j < num_squares; j++) { // Square
      tmp2_.multiplyBy(tmp2_);
    }
    for (int j = 0; j < ((ptxt_mod-1) - pow(2,num_squares)); j++) { // Mult
      tmp2_.multiplyBy(tmp_);
    }
    tmp_ = tmp2_;
    tmp_.negate();
    tmp_.addConstant(NTL::ZZX(1));
    result_ += tmp_;
  }
  return result_;
}

helib::Ctxt leq(
    helib::PubKey& public_key, helib::Ctxt& ct1_, helib::Ctxt& ct2_,
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt less_ = lt(public_key, ct1_, ct2_, ptxt_mod, slots);
  helib::Ctxt equal_ = eq(public_key, ct1_, ct2_, ptxt_mod, slots);
  helib::Ctxt tmp_(public_key), res_(public_key);

  tmp_ = less_;
  tmp_ *= equal_;
  res_ = less_;
  res_ += equal_;
  res_ -= tmp_;

  return res_;
}

helib::Ctxt lt_plain(
    helib::PubKey& public_key, helib::Ctxt& ct1_, helib::Ptxt<helib::BGV>& pt1_, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt tmp_(public_key), tmp2_(public_key), result_(public_key), ct2_(public_key);
  result_.clear();
  public_key.Encrypt(ct2_, pt1_);
  int num_squares = (int)log2(ptxt_mod-1);

  for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
    tmp_ = ct1_;
    tmp_ -= ct2_;
    tmp_.addConstant(NTL::ZZX(-i));
    tmp2_ = tmp_;
    for (int j = 0; j < num_squares; j++) { // Square
      tmp2_.multiplyBy(tmp2_);
    }
    for (int j = 0; j < ((ptxt_mod-1) - pow(2,num_squares)); j++) { // Mult
      tmp2_.multiplyBy(tmp_);
    }
    tmp_ = tmp2_;
    tmp_.negate();
    tmp_.addConstant(NTL::ZZX(1));
    result_ += tmp_;
  }
  return result_;
}

helib::Ctxt lt_plain(
    helib::PubKey& public_key, helib::Ptxt<helib::BGV>& pt1_, helib::Ctxt& ct1_,
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt tmp_(public_key), tmp2_(public_key), result_(public_key), ct2_(public_key);
  result_.clear();
  public_key.Encrypt(ct2_, pt1_);

  int num_squares = (int)log2(ptxt_mod-1);

  for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
    tmp_ = ct2_;
    tmp_ -= ct1_;
    tmp_.addConstant(NTL::ZZX(-i));
    tmp2_ = tmp_;
    for (int j = 0; j < num_squares; j++) { // Square
      tmp2_.multiplyBy(tmp2_);
    }
    for (int j = 0; j < ((ptxt_mod-1) - pow(2,num_squares)); j++) { // Mult
      tmp2_.multiplyBy(tmp_);
    }
    tmp_ = tmp2_;
    tmp_.negate();
    tmp_.addConstant(NTL::ZZX(1));
    result_ += tmp_;
  }
  return result_;
}

helib::Ctxt eq_plain(
    helib::PubKey& public_key, helib::Ctxt& ct1_, helib::Ptxt<helib::BGV>& pt1_, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt result_(public_key), tmp_(public_key), tmp2_(public_key), ct2_(public_key);
  public_key.Encrypt(ct2_, pt1_);
  int num_squares = (int)log2(ptxt_mod-1);
  tmp_ = ct1_;
  tmp_ -= ct2_;
  tmp2_ = tmp_;
  for (int i = 0; i < num_squares; i++) { // Square
    tmp2_.multiplyBy(tmp2_);
  }
  for (int i = 0; i < ((ptxt_mod-1) - pow(2,num_squares)); i++) { // Mult
    tmp2_ *= tmp_;
  }
  result_ = tmp2_;
  result_.negate();
  result_.addConstant(NTL::ZZX(1));
  return result_;
}

helib::Ctxt leq_plain(
    helib::PubKey& public_key, helib::Ctxt& ct1_, helib::Ptxt<helib::BGV>& pt1_, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt less_ = lt_plain(public_key, ct1_, pt1_, ptxt_mod, slots);
  helib::Ctxt equal_ = eq_plain(public_key, ct1_, pt1_, ptxt_mod, slots);
  helib::Ctxt tmp_(public_key), res_(public_key);

  tmp_ = less_;
  tmp_ *= equal_;
  res_ = less_;
  res_ += equal_;
  res_ -= tmp_;

  return res_;
}

helib::Ctxt leq_plain(
    helib::PubKey& public_key, helib::Ptxt<helib::BGV>& pt1_, helib::Ctxt& ct1_, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt less_ = lt_plain(public_key, pt1_, ct1_, ptxt_mod, slots);
  helib::Ctxt equal_ = eq_plain(public_key, ct1_, pt1_, ptxt_mod, slots);
  helib::Ctxt tmp_(public_key), res_(public_key);

  tmp_ = less_;
  tmp_ *= equal_;
  res_ = less_;
  res_ += equal_;
  res_ -= tmp_;

  return res_;
}
