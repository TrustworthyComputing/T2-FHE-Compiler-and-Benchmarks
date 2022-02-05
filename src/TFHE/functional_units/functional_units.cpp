#include "functional_units.hpp"

std::vector<LweSample*> e_client(std::vector<uint32_t>& ptxt_val, size_t word_sz,
                    const TFheGateBootstrappingSecretKeySet* sk) {
  size_t num_ptxt = ptxt_val.size();
  std::vector<LweSample*> result(num_ptxt);
  for (int i = 0; i < num_ptxt; i++) {
    result[i] = new_gate_bootstrapping_ciphertext_array(word_sz, sk->params);
    for (int j = 0; j < word_sz; j++) {
      bootsSymEncrypt(&result[i][j], (ptxt_val[i] >> j) & 1, sk);
    }
  }
  return result;
}

std::vector<LweSample*> e_client(uint32_t ptxt_val, size_t word_sz,
                    const TFheGateBootstrappingSecretKeySet* sk) {
  std::vector<LweSample*> result(1);
  result[0] = new_gate_bootstrapping_ciphertext_array(word_sz, sk->params);
  for (int i = 0; i < word_sz; i++) {
    bootsSymEncrypt(&result[0][i], (ptxt_val >> i) & 1, sk);
  }
  return result;
}

void copy(std::vector<LweSample*>& dst_, std::vector<LweSample*>& a_, size_t word_sz,
                               const TFheGateBootstrappingCloudKeySet* bk) {
  dst_.resize(a_.size());
  for (int i = 0; i < a_.size(); i++) {
    dst_[i] = new_gate_bootstrapping_ciphertext_array(word_sz, bk->params);
    for (int j = 0; j < word_sz; j++) {
      bootsCOPY(&dst_[i][j], &a_[i][j], bk);
    }
  }
}

std::vector<uint32_t> d_client(size_t word_sz, const std::vector<LweSample*>& ctxt,
                  const TFheGateBootstrappingSecretKeySet* sk) {
  size_t num_ptxt = ctxt.size();
  std::vector<uint32_t> ptxt(num_ptxt);
  for (int i = 0; i < num_ptxt; i++) {
    for (int j = 0; j < word_sz; j++) {
      uint32_t ri = bootsSymDecrypt(&ctxt[i][j], sk) > 0;
      ptxt[i] |= (ri << j);
    }
  }
  return ptxt;
}

std::vector<LweSample*> e_cloud(std::vector<uint32_t>& ptxt_val, size_t word_sz,
                    const TFheGateBootstrappingCloudKeySet* bk){
  size_t num_ptxt = ptxt_val.size();
  std::vector<LweSample*> result(num_ptxt);
  for (int i = 0; i < num_ptxt; i++) {
    result[i] = new_gate_bootstrapping_ciphertext_array(word_sz, bk->params);
    for (int j = 0; j < word_sz; j++) {
      bootsCONSTANT(&result[i][j], (ptxt_val[i] >> j) & 1, bk);
    }
  }
  return result;
}

std::vector<LweSample*> e_cloud(uint32_t ptxt_val, size_t word_sz,
                   const TFheGateBootstrappingCloudKeySet* bk) {
  std::vector<LweSample*> result(1);
  result[0] = new_gate_bootstrapping_ciphertext_array(word_sz, bk->params);
  for (int i = 0; i < word_sz; i++) {
    bootsCONSTANT(&result[0][i], (ptxt_val >> i) & 1, bk);
  }
  return result;
}

void shift_left_bin(std::vector<LweSample*>& result,
                    std::vector<LweSample*>& ct,
                    int amt, const size_t word_sz,
                    const TFheGateBootstrappingCloudKeySet* bk) {
  // shift left
  for (int i = 0 ; i < result.size(); i++) {
    for (int j = word_sz-1; j >= amt; j--) {
      bootsCOPY(&result[i][j], &ct[i][j-amt], bk);
    }
    for (int j = 0; j < amt; j++) {
      bootsCONSTANT(&result[i][j], 0, bk);
    }
  }
}

void shift_right_bin(std::vector<LweSample*>& result,
                     std::vector<LweSample*>& ct,
                     int amt, const size_t word_sz,
                     const TFheGateBootstrappingCloudKeySet* bk) {
  // shift right
  for (int i = 0 ; i < result.size(); i++) {
    for (int j = 0; j < word_sz-amt; j++) {
      bootsCOPY(&result[i][j], &ct[i][j+amt], bk);
    }
    // sign extension
    for (int j = word_sz-amt; j < word_sz; j++) {
      bootsCOPY(&result[i][j], &ct[i][word_sz-1], bk);
    }
  }
}

void shift_right_logical_bin(std::vector<LweSample*>& result,
                     std::vector<LweSample*>& ct,
                     int amt, const size_t word_sz,
                     const TFheGateBootstrappingCloudKeySet* bk) {
  std::vector<LweSample*> tmp(ct.size());
  for (int i = 0; i < tmp.size(); i++) {
    tmp[i] = new_gate_bootstrapping_ciphertext_array(word_sz, bk->params);
  }
  // shift right
  for (int i = 0 ; i < result.size(); i++) {
    for (int j = 0; j < word_sz-amt; j++) {
      bootsCOPY(&tmp[i][j], &ct[i][j+amt], bk);
    }
    for (int j = word_sz-amt; j < word_sz; j++) {
      bootsCONSTANT(&tmp[i][j], 0, bk);
    }
  }
  for (int i = 0; i < tmp.size(); i++) {
    for (int j = 0; j < word_sz; j++) {
      bootsCOPY(&result[i][j], &tmp[i][j], bk);
    }
  }
}

void add_mixed(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
         const std::vector<LweSample*>& b, const size_t nb_bits,
         const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* carry =
   new_gate_bootstrapping_ciphertext_array(nb_bits+1, bk->params);
  LweSample* temp = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  std::vector<LweSample*> tmp_res;
  size_t old_size = result.size();
  result.resize(a.size());
  for (int i = old_size; i < result.size(); i++) {
    result[i] = new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  }
  copy(tmp_res, result, nb_bits, bk);
  // Initialize first carry to 0.
  bootsCONSTANT(&carry[0], 0, bk);
  // Run full adders.
  for (int i = 0; i < tmp_res.size(); i++) {
    for (int j = 0; j < nb_bits; j++) {
     bootsXOR(&temp[0], &a[i][j], &b[0][j], bk);
     // Compute sum.
     bootsXOR(&tmp_res[i][j], &carry[j], &temp[0], bk);
     // Compute carry
     bootsMUX(&carry[j+1], &temp[0], &carry[j], &a[i][j], bk);
    }
  }
  copy(result, tmp_res, nb_bits, bk);
  delete_gate_bootstrapping_ciphertext_array(nb_bits+1, carry);
  delete_gate_bootstrapping_ciphertext_array(1, temp);
  for (int i = 0; i < tmp_res.size(); i++) {
   delete_gate_bootstrapping_ciphertext_array(nb_bits, tmp_res[i]);
  }
}

/// Ripple carry adder for nb_bits bits. result = a + b
void add(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
         const std::vector<LweSample*>& b, const size_t nb_bits,
         const TFheGateBootstrappingCloudKeySet* bk) {
  if (nb_bits <= 0) return ;
  size_t num_ops = std::min(a.size(), b.size());
  if ((num_ops == 1) && (a.size() != b.size())) { // batched w/ non-batched
    if (a.size() == 1) {
      add_mixed(result, b, a, nb_bits, bk);
    }
    else {
      add_mixed(result, a, b, nb_bits, bk);
    }
    return;
  }
  size_t old_size = result.size();
  result.resize(std::max(a.size(), b.size()));
  for (int i = old_size; i < result.size(); i++) {
    result[i] = new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  }
  LweSample* carry =
    new_gate_bootstrapping_ciphertext_array(nb_bits+1, bk->params);
  LweSample* temp = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  std::vector<LweSample*> tmp_res;
  copy(tmp_res, result, nb_bits, bk);
  // Initialize first carry to 0.
  bootsCONSTANT(&carry[0], 0, bk);
  // Run full adders.
  for (int i = 0; i < num_ops; i++) {
    for (int j = 0; j < nb_bits; j++) {
      bootsXOR(&temp[0], &a[i][j], &b[i][j], bk);
      // Compute sum.
      bootsXOR(&tmp_res[i][j], &carry[j], &temp[0], bk);
      // Compute carry
      bootsMUX(&carry[j+1], &temp[0], &carry[j], &a[i][j], bk);
    }
  }
  copy(result, tmp_res, nb_bits, bk);
  // Copy results if necessary
  if (a.size() != b.size()) {
    if (a.size() < b.size()) {
      for (int i = num_ops; i < b.size(); i++) {
        for (int j = 0; j < nb_bits; j++) {
            bootsCOPY(&result[i][j], &b[i][j], bk);
        }
      }
    } else {
      for (int i = num_ops; i < a.size(); i++) {
        for (int j = 0; j < nb_bits; j++) {
            bootsCOPY(&result[i][j], &a[i][j], bk);
        }
      }
    }
  }
  delete_gate_bootstrapping_ciphertext_array(nb_bits+1, carry);
  delete_gate_bootstrapping_ciphertext_array(1, temp);
  for (int i = 0; i < tmp_res.size(); i++) {
    delete_gate_bootstrapping_ciphertext_array(nb_bits, tmp_res[i]);
  }
}

void sub_mixed(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
               const std::vector<LweSample*>& b, const int nb_bits,
               const TFheGateBootstrappingCloudKeySet* bk) {
  std::vector<LweSample*> tmp_res;
  size_t old_size = result.size();
  result.resize(a.size());
  for (int i = old_size; i < result.size(); i++) {
    result[i] = new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  }
  copy(tmp_res, result, nb_bits, bk);
  LweSample* borrow = new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  LweSample* temp = new_gate_bootstrapping_ciphertext_array(3, bk->params);
  for (int i = 0; i < tmp_res.size(); i++) {
    // run half subtractor
    bootsXOR(&tmp_res[i][0], &a[i][0], &b[0][0], bk);
    bootsNOT(&temp[0], &a[i][0], bk);
    bootsAND(&borrow[0], &temp[0], &b[0][0], bk);
    // run full subtractors
    for (int j = 1; j < nb_bits; j++) {
      // Calculate difference
      bootsXOR(&temp[0], &a[i][j], &b[0][j], bk);
      bootsXOR(&tmp_res[i][j], &temp[0], &borrow[j-1], bk);
      if (j < (nb_bits-1)) {
        // Calculate borrow
        bootsNOT(&temp[1], &a[i][j], bk);
        bootsAND(&temp[2], &temp[1], &b[0][j], bk);
        bootsNOT(&temp[0], &temp[0], bk);
        bootsAND(&temp[1], &borrow[j-1], &temp[0], bk);
        bootsOR(&borrow[j], &temp[2], &temp[1], bk);
      }
    }
  }
  delete_gate_bootstrapping_ciphertext_array(nb_bits, borrow);
  delete_gate_bootstrapping_ciphertext_array(3, temp);
  for (int i = 0; i < tmp_res.size(); i++) {
    delete_gate_bootstrapping_ciphertext_array(nb_bits, tmp_res[i]);
  }
}

void sub(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
         const std::vector<LweSample*>& b, const int nb_bits,
         const TFheGateBootstrappingCloudKeySet* bk) {
  if (nb_bits <= 0) return ;
  size_t num_ops = std::min(a.size(), b.size());
  if ((num_ops == 1) && (a.size() > b.size())) {
    sub_mixed(result, a, b, nb_bits, bk);
    return;
  }
  result.resize(std::max(a.size(), b.size()));
  std::vector<LweSample*> tmp_res;
  copy(tmp_res, result, nb_bits, bk);
  LweSample* borrow = new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  LweSample* temp = new_gate_bootstrapping_ciphertext_array(3, bk->params);
  for (int i = 0; i < num_ops; i++) {
    // run half subtractor
    bootsXOR(&tmp_res[i][0], &a[i][0], &b[i][0], bk);
    bootsNOT(&temp[0], &a[i][0], bk);
    bootsAND(&borrow[0], &temp[0], &b[i][0], bk);
    // run full subtractors
    for (int j = 1; j < nb_bits; j++) {
      // Calculate difference
      bootsXOR(&temp[0], &a[i][j], &b[i][j], bk);
      bootsXOR(&tmp_res[i][j], &temp[0], &borrow[j-1], bk);
      if (j < (nb_bits-1)) {
        // Calculate borrow
        bootsNOT(&temp[1], &a[i][j], bk);
        bootsAND(&temp[2], &temp[1], &b[i][j], bk);
        bootsNOT(&temp[0], &temp[0], bk);
        bootsAND(&temp[1], &borrow[j-1], &temp[0], bk);
        bootsOR(&borrow[j], &temp[2], &temp[1], bk);
      }
    }
  }
  copy(result, tmp_res, nb_bits, bk);
  // Copy results if necessary
  if (a.size() != b.size()) {
    if (a.size() < b.size()) {
      for (int i = num_ops; i < b.size(); i++) {
        for (int j = 0; j < nb_bits; j++) {
            bootsCOPY(&result[i][j], &b[i][j], bk);
        }
      }
    } else {
      for (int i = num_ops; i < a.size(); i++) {
        for (int j = 0; j < nb_bits; j++) {
            bootsCOPY(&result[i][j], &a[i][j], bk);
        }
      }
    }
  }
  delete_gate_bootstrapping_ciphertext_array(nb_bits, borrow);
  delete_gate_bootstrapping_ciphertext_array(3, temp);
  for (int i = 0; i < tmp_res.size(); i++) {
    delete_gate_bootstrapping_ciphertext_array(nb_bits, tmp_res[i]);
  }
}

/// Ripple carry adder for nb_bits bits. result = a + b
void add_single(LweSample* result, const LweSample* a,
         const LweSample* b, const size_t nb_bits,
         const TFheGateBootstrappingCloudKeySet* bk) {
  if (nb_bits <= 0) return ;
  LweSample* carry =
    new_gate_bootstrapping_ciphertext_array(nb_bits+1, bk->params);
  LweSample* temp = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  LweSample* tmp_result = new_gate_bootstrapping_ciphertext_array(nb_bits,
                                                                  bk->params);
  // Initialize first carry to 0.
  bootsCONSTANT(&carry[0], 0, bk);
  // Run full adders.
  for (int j = 0; j < nb_bits; j++) {
    bootsXOR(&temp[0], &a[j], &b[j], bk);
    // Compute sum.
    bootsXOR(&tmp_result[j], &carry[j], &temp[0], bk);
    // Compute carry
    bootsMUX(&carry[j+1], &temp[0], &carry[j], &a[j], bk);
  }
  for (int j = 0; j < nb_bits; j++) {
    bootsCOPY(&result[j], &tmp_result[j], bk);
  }
  delete_gate_bootstrapping_ciphertext_array(nb_bits+1, carry);
  delete_gate_bootstrapping_ciphertext_array(1, temp);
  delete_gate_bootstrapping_ciphertext_array(nb_bits, tmp_result);
}

/// multiply for nb_bits bits. result = a * b
void mult_mixed(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
          const std::vector<LweSample*>& b, const size_t nb_bits,
          const TFheGateBootstrappingCloudKeySet* bk) {
  if (nb_bits <= 0) return ;
  std::vector<LweSample*> tmp_res;
  size_t old_size = result.size();
  result.resize(a.size());
  for (int i = old_size; i < result.size(); i++) {
    result[i] = new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  }
  LweSample* tmp_array =
    new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  LweSample* sum =
    new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  copy(tmp_res, result, nb_bits, bk);
  // initialize temp values to 0
  for (int i = 0; i < a.size(); ++i) {
    for (int j = 0; j < nb_bits; ++j) {
      bootsCONSTANT(&sum[j], 0, bk);
    }
    for (int j = 0; j < nb_bits; ++j) {
      for (int k = 0; k < nb_bits - j; ++k) {
        bootsAND(&tmp_array[k], &a[i][j], &b[0][k], bk);
      }
      add_single(sum + j, tmp_array, sum + j, nb_bits - j, bk);
    }
    for (int j = 0; j < nb_bits; j++) {
      bootsCOPY(&tmp_res[i][j], &sum[j], bk);
    }
  }
  copy(result, tmp_res, nb_bits, bk);
  delete_gate_bootstrapping_ciphertext_array(nb_bits, tmp_array);
  delete_gate_bootstrapping_ciphertext_array(nb_bits, sum);
  for (int i = 0; i < tmp_res.size(); i++) {
    delete_gate_bootstrapping_ciphertext_array(nb_bits, tmp_res[i]);
  }
}

/// multiply for nb_bits bits. result = a * b
void mult(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
          const std::vector<LweSample*>& b, const size_t nb_bits,
          const TFheGateBootstrappingCloudKeySet* bk) {
  if (nb_bits <= 0) return ;
  size_t num_ops = std::min(a.size(), b.size());

  if ((num_ops == 1) && (a.size() != b.size())) { // batched w/ non-batched
    if (a.size() == 1) {
      mult_mixed(result, b, a, nb_bits, bk);
    }
    else {
      mult_mixed(result, a, b, nb_bits, bk);
    }
    return;
  }
  size_t old_size = result.size();
  result.resize(std::max(a.size(), b.size()));
  for (int i = old_size; i < result.size(); i++) {
    result[i] = new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  }
  LweSample* tmp_array =
    new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  LweSample* sum =
    new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  std::vector<LweSample*> tmp_res;
  copy(tmp_res, result, nb_bits, bk);
  // initialize temp values to 0
  for (int i = 0; i < num_ops; ++i) {
    for (int j = 0; j < nb_bits; ++j) {
      bootsCONSTANT(&sum[j], 0, bk);
    }
    for (int j = 0; j < nb_bits; ++j) {
      for (int k = 0; k < nb_bits - j; ++k) {
        bootsAND(&tmp_array[k], &a[i][j], &b[i][k], bk);
      }
      add_single(sum + j, tmp_array, sum + j, nb_bits - j, bk);
    }
    for (int j = 0; j < nb_bits; j++) {
      bootsCOPY(&tmp_res[i][j], &sum[j], bk);
    }
  }
  copy(result, tmp_res, nb_bits, bk);
  // Copy results if necessary
  if (a.size() != b.size()) {
    if (a.size() < b.size()) {
      for (int i = num_ops; i < b.size(); i++) {
        for (int j = 0; j < nb_bits; j++) {
            bootsCOPY(&result[i][j], &b[i][j], bk);
        }
      }
    } else {
      for (int i = num_ops; i < a.size(); i++) {
        for (int j = 0; j < nb_bits; j++) {
            bootsCOPY(&result[i][j], &a[i][j], bk);
        }
      }
    }
  }
  delete_gate_bootstrapping_ciphertext_array(nb_bits, tmp_array);
  delete_gate_bootstrapping_ciphertext_array(nb_bits, sum);
  for (int i = 0; i < tmp_res.size(); i++) {
    delete_gate_bootstrapping_ciphertext_array(nb_bits, tmp_res[i]);
  }
}

/// Increment ciphertext a by 1. result = a + 1.
void inc(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
         const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {
  if (nb_bits <= 0) return ;
  size_t num_ops = a.size();
  result.resize(a.size());
  LweSample* carry =
    new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  LweSample* temp = new_gate_bootstrapping_ciphertext(bk->params);
  for (int i = 0; i < num_ops; i++) {
    bootsCONSTANT(&carry[0], 1, bk);
    for (int j = 0; j < (nb_bits - 1); j++) {
      bootsXOR(temp, &carry[j], &a[i][j], bk);
      bootsAND(&carry[j+1], &carry[j], &a[i][j], bk);
      bootsCOPY(&result[i][j], temp, bk);
    }
    bootsXOR(&result[i][nb_bits-1], &carry[nb_bits-1], &a[i][nb_bits-1], bk);
  }
  delete_gate_bootstrapping_ciphertext_array(nb_bits, carry);
}

/// Incrementer circuit: result++.
void inc_inplace(std::vector<LweSample*>& result, const size_t nb_bits,
                 const TFheGateBootstrappingCloudKeySet* bk) {
  inc(result, result, nb_bits, bk);
}

/// Equality check. result = a == b
void eq(std::vector<LweSample*>& result_, const std::vector<LweSample*>& a,
        const std::vector<LweSample*>& b, const size_t word_sz,
        const TFheGateBootstrappingCloudKeySet* bk) {
  assert(("Result ciphertext should not be any of the equality arguments",
          result_ != a && result_ != b));
  if (word_sz <= 0) return ;
  size_t num_ops = std::min(a.size(), b.size());
  result_.resize(std::max(a.size(), b.size()));
  LweSample* tmp_ = new_gate_bootstrapping_ciphertext_array(word_sz, bk->params);
  // Compute XNORs across a and b and AND all results together.
  for (int i = 0; i < num_ops; i++) {
    bootsCONSTANT(&result_[i][0], 1, bk);
    for (int j = 0; j < word_sz; j++) {
      bootsXNOR(&tmp_[j], &a[i][j], &b[i][j], bk);
      bootsAND(&result_[i][0], &result_[i][0], &tmp_[j], bk);
    }
  }
  // Copy results if necessary
  if (a.size() != b.size()) {
    if (a.size() < b.size()) {
      for (int i = num_ops; i < b.size(); i++) {
        bootsCONSTANT(&result_[i][0], 0, bk);
      }
    } else {
      for (int i = num_ops; i < a.size(); i++) {
        bootsCONSTANT(&result_[i][0], 0, bk);
      }
    }
  }
  delete_gate_bootstrapping_ciphertext_array(word_sz, tmp_);
}

void neq(std::vector<LweSample*>& result_, const std::vector<LweSample*>& a,
        const std::vector<LweSample*>& b, const size_t word_sz,
        const TFheGateBootstrappingCloudKeySet* bk) {
  eq(result_, a, b, word_sz, bk);
  for (int i = 0; i < result_.size(); i++) {
    bootsNOT(&result_[i][0], &result_[i][0], bk);
  }
}

/// Less than. result = a < b
void lt(std::vector<LweSample*>& result_, const std::vector<LweSample*>& a,
        const std::vector<LweSample*>& b, const size_t word_sz,
        const TFheGateBootstrappingCloudKeySet* bk) {
  if (word_sz <= 0) return ;
  size_t num_ops = std::min(a.size(), b.size());
  result_.resize(std::max(a.size(), b.size()));
  LweSample* n1_ = new_gate_bootstrapping_ciphertext(bk->params);
  LweSample* n2_ = new_gate_bootstrapping_ciphertext(bk->params);
  LweSample* n1_AND_n2_ = new_gate_bootstrapping_ciphertext(bk->params);
  assert(("Result ciphertext should not be any of the equality arguments",
          result_ != a && result_ != b));
  for (int i = 0; i < num_ops; i++) {
    bootsCONSTANT(&result_[i][0], 0, bk);
    for (int j = 0; j < word_sz; ++j) {
      bootsXOR(n1_, &result_[i][0], &a[i][j], bk);
      bootsXOR(n2_, &result_[i][0], &b[i][j], bk);
      bootsAND(n1_AND_n2_, n1_, n2_, bk);
      bootsXOR(&result_[i][0], n1_AND_n2_, &b[i][j], bk);
    }
  }
  // Copy results if necessary
  if (a.size() != b.size()) {
    if (a.size() < b.size()) {
      for (int i = num_ops; i < b.size(); i++) {
        bootsCONSTANT(&result_[i][0], 0, bk);
      }
    } else {
      for (int i = num_ops; i < a.size(); i++) {
        bootsCONSTANT(&result_[i][0], 0, bk);
      }
    }
  }
  delete_gate_bootstrapping_ciphertext(n1_);
  delete_gate_bootstrapping_ciphertext(n2_);
  delete_gate_bootstrapping_ciphertext(n1_AND_n2_);
}

/// LTE check. result = a <= b
void leq(std::vector<LweSample*>& result_, const std::vector<LweSample*>& a,
        const std::vector<LweSample*>& b, const size_t word_sz,
        const TFheGateBootstrappingCloudKeySet* bk) {
  if (word_sz <= 0) return ;
  size_t num_ops = std::min(a.size(), b.size());
  result_.resize(std::max(a.size(), b.size()));
  lt(result_, b, a, word_sz, bk);
  for (int i = 0; i < result_.size(); i++) {
    bootsNOT(&result_[i][0], &result_[i][0], bk);
  }
  // Copy results if necessary
  if (a.size() != b.size()) {
    if (a.size() < b.size()) {
      for (int i = num_ops; i < b.size(); i++) {
        bootsCONSTANT(&result_[i][0], 0, bk);
      }
    } else {
      for (int i = num_ops; i < a.size(); i++) {
        bootsCONSTANT(&result_[i][0], 0, bk);
      }
    }
  }
}

void e_not(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
           const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {
  size_t num_ops = a.size();
  result.resize(a.size());
  for (int i = 0; i < num_ops; i++) {
    for (int j = 0; j < nb_bits; j++) {
      bootsNOT(&result[i][j], &a[i][j], bk);
    }
  }
}

void e_xor(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
           const std::vector<LweSample*>& b, const size_t nb_bits,
           const TFheGateBootstrappingCloudKeySet* bk) {
  size_t num_ops = std::min(a.size(), b.size());
  result.resize(std::max(a.size(), b.size()));
  for (int i = 0; i < num_ops; i++) {
    for (int j = 0; j < nb_bits; j++) {
    bootsXOR(&result[i][j], &a[i][j], &b[i][j], bk);
    }
  }
  // Copy results if necessary
  if (a.size() != b.size()) {
    if (a.size() < b.size()) {
      for (int i = num_ops; i < b.size(); i++) {
        for (int j = 0; j < nb_bits; j++) {
            bootsCOPY(&result[i][j], &b[i][j], bk);
        }
      }
    } else {
      for (int i = num_ops; i < a.size(); i++) {
        for (int j = 0; j < nb_bits; j++) {
            bootsCOPY(&result[i][j], &a[i][j], bk);
        }
      }
    }
  }
}

void e_mux(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
           const std::vector<LweSample*>& b, const std::vector<LweSample*>& c,
           const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {
  size_t num_ops = std::min(b.size(), c.size());
  assert(a.size() >= num_ops);
  result.resize(std::max(b.size(), c.size()));
  for (int i = 0; i < num_ops; i++) {
    for (int j = 0; j < nb_bits; j++) {
      bootsMUX(&result[i][j], &a[i][0], &b[i][j], &c[i][j], bk);
    }
  }
  // Copy results if necessary
  if (a.size() != b.size()) {
    if (a.size() < b.size()) {
      for (int i = num_ops; i < b.size(); i++) {
        for (int j = 0; j < nb_bits; j++) {
            bootsCOPY(&result[i][j], &b[i][j], bk);
        }
      }
    } else {
      for (int i = num_ops; i < a.size(); i++) {
        for (int j = 0; j < nb_bits; j++) {
            bootsCOPY(&result[i][j], &a[i][j], bk);
        }
      }
    }
  }
}
