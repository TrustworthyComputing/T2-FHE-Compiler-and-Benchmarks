#include "helper.hpp"
#include <iostream>

bool is_pow_of_2(int n) {
  int count = 0;
  for (int i = 0; i < 32; i++){
    count += (n >> i & 1);
  }
  return count == 1 && n > 0;
}

LweSample* enc_cloud(uint32_t ptxt_val, uint32_t word_sz,
                     const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* result =
    new_gate_bootstrapping_ciphertext_array(word_sz, bk->params);
  for (int i = 0; i < word_sz; i++) {
    bootsCONSTANT(&result[i], (ptxt_val >> i) & 1, bk);
  }
  return result;
}

LweSample* cmp(const LweSample* a, const LweSample* b, const uint32_t word_sz,
               const TFheGateBootstrappingCloudKeySet* bk) {
  LweSample* not_a = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  LweSample* not_b = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  LweSample* temp = new_gate_bootstrapping_ciphertext_array(10, bk->params);
  LweSample* gt = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  LweSample* lt = new_gate_bootstrapping_ciphertext_array(1, bk->params);
  LweSample* result = new_gate_bootstrapping_ciphertext_array(1, bk->params);
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
  return result;
}
