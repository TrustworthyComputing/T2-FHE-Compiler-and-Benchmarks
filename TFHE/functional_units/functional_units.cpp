#include "functional_units.hpp"

LweSample* e_cloud(uint32_t ptxt_val, size_t word_sz,
                     const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* result =
    new_gate_bootstrapping_ciphertext_array(word_sz, bk->params);
  for (int i = 0; i < word_sz; i++) {
    bootsCONSTANT(&result[i], (ptxt_val >> i) & 1, bk);
  }
  return result;
}

void rotate_inplace(LweSample* result, rotation_t dir, int amt,
                    const size_t word_sz, 
                    const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* tmp = 
    new_gate_bootstrapping_ciphertext_array(word_sz, bk->params);

  if (dir == LEFT) {
    // rotate left
    for (int i = 0; i < word_sz; i++) {
      bootsCOPY(&tmp[i], &result[(i-amt) % word_sz], bk);
    }
  } else {
    // rotate right
    for (int i = 0; i < word_sz; i++) {
      bootsCOPY(&tmp[i], &result[(i+amt) % word_sz], bk);
    }
  }

  for (int i = 0; i < word_sz; i++) {
    bootsCOPY(&result[i], &tmp[i], bk);
  }

  delete_gate_bootstrapping_ciphertext_array(word_sz, tmp);
}

/// Ripple carry adder for nb_bits bits. result = a + b
void add(LweSample* result, const LweSample* a, const LweSample* b,
           const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {
  if (nb_bits <= 0) return ;
  LweSample* carry =
    new_gate_bootstrapping_ciphertext_array(nb_bits+1, bk->params);
  LweSample* temp = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  // Initialize first carry to 0.
  bootsCONSTANT(&carry[0], 0, bk);

  // Run full adders.
  for (int i = 0; i < nb_bits; i++) {
    bootsXOR(&temp[0], &a[i], &b[i], bk);
    // Compute sum.
    bootsXOR(&result[i], &carry[i], &temp[0], bk);
    // Compute carry
    bootsMUX(&carry[i+1], &temp[0], &carry[i], &a[i], bk);
  }

  delete_gate_bootstrapping_ciphertext_array(nb_bits+1, carry);
  delete_gate_bootstrapping_ciphertext_array(1, temp);
}

void sub(LweSample* result, const LweSample* a, const LweSample* b, const int nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {

    LweSample* borrow = new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
    LweSample* temp = new_gate_bootstrapping_ciphertext_array(3, bk->params);

    // run half subtractor
    bootsXOR(&result[0], &a[0], &b[0], bk);
    bootsNOT(&temp[0], &a[0], bk);
    bootsAND(&borrow[0], &temp[0], &b[0], bk);

    // run full subtractors
    for (int i = 1; i < nb_bits; i++) {

      // Calculate difference
      bootsXOR(&temp[0], &a[i], &b[i], bk);
      bootsXOR(&result[i], &temp[0], &borrow[i-1], bk);

      if (i < (nb_bits-1)) {
        // Calculate borrow
        bootsNOT(&temp[1], &a[i], bk);
        bootsAND(&temp[2], &temp[1], &b[i], bk);
        bootsNOT(&temp[0], &temp[0], bk);
        bootsAND(&temp[1], &borrow[i-1], &temp[0], bk);
        bootsOR(&borrow[i], &temp[2], &temp[1], bk);
      }
    }

    delete_gate_bootstrapping_ciphertext_array(nb_bits, borrow);
    delete_gate_bootstrapping_ciphertext_array(3, temp);
}

/// multiply for nb_bits bits. result = a * b
void mult(LweSample* result, const LweSample* a, const LweSample* b,
          const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {
  if (nb_bits <= 0) return ;
  LweSample* tmp_array =
    new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  LweSample* sum =
    new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  // initialize temp values to 0
  for (int i = 0; i < nb_bits; ++i) {
    bootsCONSTANT(&sum[i], 0, bk);
  }

  for (int i = 0; i < nb_bits; ++i) {
    for (int j = 0; j < nb_bits - i; ++j) {
      bootsAND(&tmp_array[j], &a[i], &b[j], bk);
    }
    add(sum + i, tmp_array, sum + i, nb_bits - i, bk);
  }
  for (int j = 0; j < nb_bits; j++) {
    bootsCOPY(&result[j], &sum[j], bk);
  }

  delete_gate_bootstrapping_ciphertext_array(nb_bits, tmp_array);
  delete_gate_bootstrapping_ciphertext_array(nb_bits, sum);
}

/// Increment ciphertext a by 1. result = a + 1.
void inc(LweSample* result, const LweSample* a, const size_t nb_bits,
         const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* carry =
    new_gate_bootstrapping_ciphertext_array(nb_bits, bk->params);
  bootsCONSTANT(&carry[0], 1, bk);
  LweSample* temp = new_gate_bootstrapping_ciphertext(bk->params);

  for (int i = 0; i < (nb_bits - 1); i++) {
    bootsXOR(temp, &carry[i], &a[i], bk);
    bootsAND(&carry[i+1], &carry[i], &a[i], bk);
    bootsCOPY(&result[i], temp, bk);
  }
  bootsXOR(&result[nb_bits-1], &carry[nb_bits-1], &a[nb_bits-1], bk);

  delete_gate_bootstrapping_ciphertext_array(nb_bits, carry);
}

/// Increment ciphertext a by 1 and store result to a. a++.
void inc_inplace(LweSample* a, const size_t nb_bits,
                 const TFheGateBootstrappingCloudKeySet* bk) {
  inc(a, a, nb_bits, bk);
}

/// Equality check. result = a == b
void eq(LweSample* result_, const LweSample* a, const LweSample* b,
        const size_t word_sz, const TFheGateBootstrappingCloudKeySet* bk) {
  assert(("Result ciphertext should not be any of the equality arguments",
          result_ != a && result_ != b));
  LweSample* tmp_ = new_gate_bootstrapping_ciphertext_array(word_sz, bk->params);
  // Compute XNORs across a and b and AND all results together.
  bootsCONSTANT(&result_[0], 1, bk);
  for (int i = 0; i < word_sz; i++) {
    bootsXNOR(&tmp_[i], &a[i], &b[i], bk);
    bootsAND(&result_[0], &result_[0], &tmp_[i], bk);
  }
}

/// Less or equal than. result = a <= b
void leq(LweSample* result_, const LweSample* a, const LweSample* b,
         const size_t word_sz, const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* n1_ = new_gate_bootstrapping_ciphertext(bk->params);
  LweSample* n2_ = new_gate_bootstrapping_ciphertext(bk->params);
  LweSample* n1_AND_n2_ = new_gate_bootstrapping_ciphertext(bk->params);
  assert(("Result ciphertext should not be any of the equality arguments",
          result_ != a && result_ != b));
  bootsCONSTANT(result_, 0, bk);
  for (int i = 0; i < word_sz; ++i) {
    bootsXOR(n1_, result_, &a[i], bk);
    bootsXOR(n2_, result_, &b[i], bk);
    bootsAND(n1_AND_n2_, n1_, n2_, bk);
    bootsXOR(result_, n1_AND_n2_, &b[i], bk);
  }

  delete_gate_bootstrapping_ciphertext(n1_);
  delete_gate_bootstrapping_ciphertext(n2_);
  delete_gate_bootstrapping_ciphertext(n1_AND_n2_);
}

/// Less than. result = a < b
void lt(LweSample* result_, const LweSample* a, const LweSample* b,
         const size_t word_sz, const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* not_a_ = new_gate_bootstrapping_ciphertext(bk->params);
  LweSample* tmp_ = new_gate_bootstrapping_ciphertext(bk->params);
  LweSample* tmp_result_ = new_gate_bootstrapping_ciphertext(bk->params);
  for (int i = 0; i < word_sz; ++i) {
    bootsNOT(not_a_, &a[i], bk);
    bootsAND(tmp_, not_a_, &b[i], bk);
    if (i == 0) {
      bootsCOPY(tmp_result_, tmp_, bk);
    }
    else {
      bootsAND(tmp_result_, tmp_result_, tmp_, bk);
    }
  }
  bootsCOPY(result_, tmp_result_, bk);
  delete_gate_bootstrapping_ciphertext(not_a_);
  delete_gate_bootstrapping_ciphertext(tmp_);
  delete_gate_bootstrapping_ciphertext(tmp_result_);
}

/// Greater than. result = a > b
void gt(LweSample* result_, const LweSample* a, const LweSample* b,
         const size_t word_sz, const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* not_b_ = new_gate_bootstrapping_ciphertext(bk->params);
  LweSample* tmp_ = new_gate_bootstrapping_ciphertext(bk->params);
  LweSample* tmp_result_ = new_gate_bootstrapping_ciphertext(bk->params);
  for (int i = 0; i < word_sz; ++i) {
    bootsNOT(not_b_, &b[i], bk);
    bootsAND(tmp_, not_b_, &a[i], bk);
    if (i == 0) {
      bootsCOPY(tmp_result_, tmp_, bk);
    }
    else {
      bootsAND(tmp_result_, tmp_result_, tmp_, bk);
    }
  }
  bootsCOPY(result_, tmp_result_, bk);
  delete_gate_bootstrapping_ciphertext(not_b_);
  delete_gate_bootstrapping_ciphertext(tmp_);
  delete_gate_bootstrapping_ciphertext(tmp_result_);
}

void e_not(LweSample* result, const LweSample* a, const size_t nb_bits, 
           const TFheGateBootstrappingCloudKeySet* bk) {
  for (int i = 0; i < nb_bits; i++) {
    bootsNOT(&result[i], &a[i], bk);
  }
}

void e_and(LweSample* result, const LweSample* a, const LweSample* b,
           const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {
  for (int i = 0; i < nb_bits; i++) {
    bootsAND(&result[i], &a[i], &b[i], bk);
  }
}

void e_or(LweSample* result, const LweSample* a, const LweSample* b,
          const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {
  for (int i = 0; i < nb_bits; i++) {
    bootsOR(&result[i], &a[i], &b[i], bk);
  }
}

void e_nand(LweSample* result, const LweSample* a, const LweSample* b,
         const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {
  for (int i = 0; i < nb_bits; i++) {
    bootsNAND(&result[i], &a[i], &b[i], bk);
  }
}

void e_nor(LweSample* result, const LweSample* a, const LweSample* b,
         const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {
  for (int i = 0; i < nb_bits; i++) {
    bootsNOR(&result[i], &a[i], &b[i], bk);
  }
}

void e_xor(LweSample* result, const LweSample* a, const LweSample* b,
         const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {
  for (int i = 0; i < nb_bits; i++) {
    bootsXOR(&result[i], &a[i], &b[i], bk);
  }
}

void e_xnor(LweSample* result, const LweSample* a, const LweSample* b,
         const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk) {
  for (int i = 0; i < nb_bits; i++) {
    bootsXNOR(&result[i], &a[i], &b[i], bk);
  }
}

void e_mux(LweSample* result, const LweSample* a, const LweSample* b,
           const LweSample* c, const size_t nb_bits, 
           const TFheGateBootstrappingCloudKeySet* bk) {
  for (int i = 0; i < nb_bits; i++) {
    bootsMUX(&result[i], &a[i], &b[i], &c[i], bk);
  }
}

LweSample* e_cloud_int(int32_t ptxt_val, uint32_t ptxt_mod, 
                 const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* result = new_LweSample(bk->params->in_out_params);
  const Torus32 mu = modSwitchToTorus32(ptxt_val, ptxt_mod);
  lweNoiselessTrivial(result, mu, bk->params->in_out_params);
  return result;
}

LweSample* e_bin_to_int(LweSample* a, uint32_t ptxt_mod,
                        const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* result = new_LweSample(bk->params->in_out_params);
  const Torus32 mu = modSwitchToTorus32(-1, ptxt_mod);
  tfhe_bootstrap_FFT(result, bk->bkFFT, mu, a);
  return result;
}

LweSample* e_int_to_bin(LweSample* a,
                        const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* result = new_LweSample(bk->params->in_out_params);
  const Torus32 mu = modSwitchToTorus32(-1,8);
  tfhe_bootstrap_FFT(result, bk->bkFFT, mu, a);
  return result;
}

void add_int(LweSample* result, const LweSample* a, const LweSample* b,
             const TFheGateBootstrappingCloudKeySet* bk) {
  const int32_t n = bk->params->in_out_params->n;
  for (int32_t i = 0; i < n; ++i) { 
    result->a[i] = a->a[i] + b->a[i];
  }
  result->b = a->b + b->b;
  result->current_variance = a->current_variance + b->current_variance; 
}

void sub_int(LweSample* result, const LweSample* a, const LweSample* b,
             const TFheGateBootstrappingCloudKeySet* bk) {
  const int32_t n = bk->params->in_out_params->n;
  for (int32_t i = 0; i < n; ++i) { 
    result->a[i] = a->a[i] - b->a[i];
  }
  result->b = a->b - b->b;
  result->current_variance = a->current_variance + b->current_variance; 
}

void mult_plain_int(LweSample* result, const LweSample* a, int32_t p,
             const TFheGateBootstrappingCloudKeySet* bk) {
  const int32_t n = bk->params->in_out_params->n;
  for (int32_t i = 0; i < n; ++i) {
    result->a[i] = p*a->a[i];
  }
  result->b += p*a->b;
  result->current_variance += (p*p)*a->current_variance; 
}