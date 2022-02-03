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

helib::Ctxt exor(helib::PubKey& public_key, helib::Ctxt& ctxt_1, 
                 helib::Ctxt& ctxt_2) {
  helib::Ctxt result(public_key);
  result = ctxt_1;
  result -= ctxt_2;
  result.multiplyBy(result);
  return result;
}

std::vector<helib::Ctxt> add_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  std::vector<helib::Ctxt> res_;
  helib::CtPtrs_vectorCt output_wrapper(res_);
  helib::addTwoNumbers(
      output_wrapper,
      helib::CtPtrs_vectorCt(c1),
      helib::CtPtrs_vectorCt(c2),
      c1.size(), &unpackSlotEncoding);
  return ctptr_to_vec(public_key, output_wrapper);
}

std::vector<helib::Ctxt> shift_left_bin(helib::PubKey& public_key,
                                        std::vector<helib::Ctxt>& ct,
                                        size_t amt) {
  assert(amt < ct.size());
  helib::Ctxt scratch(public_key);
  std::vector<helib::Ctxt> res_(ct.size(), scratch);
  // shift data (LSB is at 0, MSB is at size - 1)
  for (int i = ct.size() - amt - 1; i >= 0; --i) {
    res_[i + amt] = ct[i];
  }
  // Fill with zeros
  for (int i = amt - 1; i >= 0; --i) {
    res_[i].clear();
  }
  return res_;
}

std::vector<helib::Ctxt> shift_right_bin(helib::PubKey& public_key,
                                         std::vector<helib::Ctxt>& ct, 
                                         size_t amt) {
  assert(amt < ct.size());
  helib::Ctxt scratch(public_key);
  std::vector<helib::Ctxt> res_(ct.size(), scratch);
  // Shift data (LSB is at 0, MSB is at size - 1)
  for (int i = amt; i < ct.size(); ++i) {
    res_[i - amt] = ct[i];
  }
  // Extend sign bit
  for (int i = ct.size() - amt; i < ct.size(); ++i) {
    res_[i] = ct[ ct.size() - 1 ];
  }
  return res_;
}

std::vector<helib::Ctxt> shift_right_logical_bin(helib::PubKey& public_key,
                                                 std::vector<helib::Ctxt>& ct, 
                                                 size_t amt) {
  assert(amt < ct.size());
  helib::Ctxt scratch(public_key);
  std::vector<helib::Ctxt> res_(ct.size(), scratch);
  // Initialize with zeros
  for (int i = amt; i < ct.size(); ++i) {
    res_[i].clear();
  }
  // shift data (LSB is at 0, MSB is at size - 1)
  for (int i = amt; i < ct.size(); ++i) {
    res_[i - amt] = ct[i];
  }
  return res_;
}

std::vector<helib::Ctxt> not_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& ct) {
  helib::Ctxt scratch(public_key);
  std::vector<helib::Ctxt> res_(ct.size(), scratch);
  for (int i = 0; i < res_.size(); i++) {
    res_[i] = ct[i];
    res_[i].negate();
    res_[i].addConstant(NTL::ZZX(1));
  }
  return res_;
}

std::vector<helib::Ctxt> xor_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 size_t ptxt_mod) {
  assert(c1.size() == c2.size());
  helib::Ctxt scratch(public_key);
  std::vector<helib::Ctxt> res_(c1.size(), scratch);
  if (ptxt_mod > 2) {
    for (int i = 0; i < res_.size(); i++) {
      res_[i] = c1[i];
      res_[i] -= c2[i];
      res_[i] *= res_[i];
    }
  } else {
    for (int i = 0; i < res_.size(); i++) {
      res_[i] = c1[i];
      res_[i] += c2[i];
    }
  }
  return res_;
}

std::vector<helib::Ctxt> sub_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  helib::Ctxt scratch(public_key);
  std::vector<helib::Ctxt> res_(c1.size(), scratch);
  helib::CtPtrs_vectorCt diff(res_);
  helib::subtractBinary(
      diff,
      helib::CtPtrs_vectorCt(c1),
      helib::CtPtrs_vectorCt(c2),
      &unpackSlotEncoding);
  
  return ctptr_to_vec(public_key, diff);
}

std::vector<helib::Ctxt> mult_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  std::vector<helib::Ctxt> res_;
  helib::CtPtrs_vectorCt prod(res_);
  helib::multTwoNumbers(
      prod,
      helib::CtPtrs_vectorCt(c1),
      helib::CtPtrs_vectorCt(c2),
      false, c1.size(), &unpackSlotEncoding);
  return ctptr_to_vec(public_key, prod);
}

std::vector<helib::Ctxt> eq_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  helib::Ctxt mu_(public_key), ni_(public_key);
  helib::compareTwoNumbers(mu_, ni_, helib::CtPtrs_vectorCt(c1), 
                           helib::CtPtrs_vectorCt(c2), false, 
                           &unpackSlotEncoding);
  mu_.negate();
  mu_.addConstant(NTL::ZZX(1));
  ni_.negate();
  ni_.addConstant(NTL::ZZX(1));
  mu_.multiplyBy(ni_);
  std::vector<helib::Ctxt> res_(c1.size(), mu_);
  for (size_t i = 1; i < res_.size(); ++i) {
    res_[i].clear();
  }
  return res_;
}

std::vector<helib::Ctxt> neq_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  std::vector<helib::Ctxt> res_ = eq_bin(public_key, c1, c2, 
      unpackSlotEncoding, slots);
  res_[0].negate();
  res_[0].addConstant(NTL::ZZX(1));
  return res_;
}

std::vector<helib::Ctxt> lt_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  helib::Ctxt mu_(public_key), ni_(public_key);
  helib::compareTwoNumbers(mu_, ni_, helib::CtPtrs_vectorCt(c1), 
                           helib::CtPtrs_vectorCt(c2), false, 
                           &unpackSlotEncoding);

  std::vector<helib::Ctxt> res_(c1.size(), ni_);
  for (size_t i = 1; i < res_.size(); ++i) {
    res_[i].clear();
  }
  return res_;
}

std::vector<helib::Ctxt> leq_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots) {
  helib::Ctxt mu_(public_key), ni_(public_key);
  helib::compareTwoNumbers(mu_, ni_, helib::CtPtrs_vectorCt(c1), 
                           helib::CtPtrs_vectorCt(c2), false, 
                           &unpackSlotEncoding);
  mu_.negate();
  mu_.addConstant(NTL::ZZX(1));
  std::vector<helib::Ctxt> res_(c1.size(), mu_);
  for (size_t i = 1; i < res_.size(); ++i) {
    res_[i].clear();
  }
  return res_;
}

helib::Ctxt eq(
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ctxt& c2, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt result_(public_key), tmp_(public_key), tmp2_(public_key);
  int num_squares = (int)log2(ptxt_mod-1);
  tmp_ = c1;
  tmp_ -= c2;
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

helib::Ctxt neq(
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ctxt& c2, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt res_ = eq(public_key, c1, c2, ptxt_mod, slots);
  res_.negate();
  res_.addConstant(NTL::ZZX(1));
  return res_;
}

helib::Ctxt lt(
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ctxt& c2, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt tmp_(public_key), tmp2_(public_key), result_(public_key);
  result_.clear();
  int num_squares = (int)log2(ptxt_mod-1);
  for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
    tmp_ = c1;
    tmp_ -= c2;
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
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ctxt& c2,
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt less_ = lt(public_key, c1, c2, ptxt_mod, slots);
  helib::Ctxt equal_ = eq(public_key, c1, c2, ptxt_mod, slots);
  helib::Ctxt tmp_(public_key), res_(public_key);
  tmp_ = less_;
  tmp_ *= equal_;
  res_ = less_;
  res_ += equal_;
  res_ -= tmp_;
  return res_;
}

helib::Ctxt lt_plain(
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ptxt<helib::BGV>& pt1, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt tmp_(public_key), tmp2_(public_key), result_(public_key), c2(public_key);
  result_.clear();
  public_key.Encrypt(c2, pt1);
  int num_squares = (int)log2(ptxt_mod-1);
  for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
    tmp_ = c1;
    tmp_ -= c2;
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
    helib::PubKey& public_key, helib::Ptxt<helib::BGV>& pt1, helib::Ctxt& c1,
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt tmp_(public_key), tmp2_(public_key), result_(public_key), c2(public_key);
  result_.clear();
  public_key.Encrypt(c2, pt1);
  int num_squares = (int)log2(ptxt_mod-1);
  for (int i = -(ptxt_mod-1)/2; i < 0; i++) {
    tmp_ = c2;
    tmp_ -= c1;
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
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ptxt<helib::BGV>& pt1, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt result_(public_key), tmp_(public_key), tmp2_(public_key), c2(public_key);
  public_key.Encrypt(c2, pt1);
  int num_squares = (int)log2(ptxt_mod-1);
  tmp_ = c1;
  tmp_ -= c2;
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

helib::Ctxt neq_plain(
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ptxt<helib::BGV>& pt1, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt res_ = eq_plain(public_key, c1, pt1, ptxt_mod, slots);
  res_.negate();
  res_.addConstant(NTL::ZZX(1));
  return res_;
}

helib::Ctxt leq_plain(
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ptxt<helib::BGV>& pt1, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt less_ = lt_plain(public_key, c1, pt1, ptxt_mod, slots);
  helib::Ctxt equal_ = eq_plain(public_key, c1, pt1, ptxt_mod, slots);
  helib::Ctxt tmp_(public_key), res_(public_key);
  tmp_ = less_;
  tmp_ *= equal_;
  res_ = less_;
  res_ += equal_;
  res_ -= tmp_;
  return res_;
}

helib::Ctxt leq_plain(
    helib::PubKey& public_key, helib::Ptxt<helib::BGV>& pt1, helib::Ctxt& c1, 
    size_t ptxt_mod, size_t slots) {
  helib::Ctxt less_ = lt_plain(public_key, pt1, c1, ptxt_mod, slots);
  helib::Ctxt equal_ = eq_plain(public_key, c1, pt1, ptxt_mod, slots);
  helib::Ctxt tmp_(public_key), res_(public_key);
  tmp_ = less_;
  tmp_ *= equal_;
  res_ = less_;
  res_ += equal_;
  res_ -= tmp_;
  return res_;
}
