#include "helper.hpp"

bool is_pow_of_2(int n) {
  int count = 0;
  for (int i = 0; i < 32; i++){
    count += (n >> i & 1);
  }
  return count == 1 && n > 0;
}

LweSample* enc_cloud(uint32_t ptxt_val, size_t word_sz,
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

/// Adder circuit: a += b.
void add_inplace(LweSample* a, const LweSample* b, const size_t nb_bits,
                 const TFheGateBootstrappingCloudKeySet* bk) {
  add(a, a, b, nb_bits, bk);
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

/// Multiplier circuit: a *= b.
void mult_inplace(LweSample* a, const LweSample* b, const size_t nb_bits,
                      const TFheGateBootstrappingCloudKeySet* bk) {
  mult(a, a, b, nb_bits, bk);
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

void cmp(LweSample* result, const LweSample* a, const LweSample* b,
         const size_t word_sz, const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* not_a = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  LweSample* not_b = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  LweSample* temp = new_gate_bootstrapping_ciphertext_array(10, bk->params);
  LweSample* gt = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  LweSample* lt = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  // Initialize cascading inputs.
  bootsCONSTANT(gt, 1, bk);
  bootsCONSTANT(&lt[0], 1, bk);
  bootsCONSTANT(&result[0], 1, bk);

  // Run 1 bit comparators.
  for (int i = word_sz - 1; i >= 0; i--) {
    // Invert inputs.
    bootsNOT(&not_a[0], &a[i], bk);
    bootsNOT(&not_b[0], &b[i], bk);
    // Compute greater than path.
    bootsNOT(&temp[0], &gt[0], bk);
    bootsNAND(&temp[1], &a[i], &not_b[0], bk);
    bootsNAND(&temp[2], &temp[1], &result[0], bk);
    bootsNOT(&temp[3], &temp[2], bk);
    bootsNAND(&gt[0], &temp[0], &temp[3], bk);
    bootsNOT(&temp[8], &lt[0], bk);
    bootsAND(&gt[0], &temp[8], &gt[0], bk);
    // Compute less than path.
    bootsNOT(&temp[4], &lt[0], bk);
    bootsNAND(&temp[5], &not_a[0], &b[i], bk);
    bootsNAND(&temp[6], &temp[5], &result[0], bk);
    bootsNOT(&temp[7], &temp[6], bk);
    bootsNAND(&lt[0], &temp[7], &temp[4], bk);
    bootsNOT(&temp[9], &gt[0], bk);
    bootsAND(&lt[0], &temp[9], &lt[0], bk);
    // Compute equality path.
    bootsNOR(&result[0], &gt[0], &lt[0], bk);
  }

  delete_gate_bootstrapping_ciphertext_array(1, not_a);
  delete_gate_bootstrapping_ciphertext_array(1, not_b);
  delete_gate_bootstrapping_ciphertext_array(8, temp);
  delete_gate_bootstrapping_ciphertext_array(1, gt);
  delete_gate_bootstrapping_ciphertext_array(1, lt);
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
