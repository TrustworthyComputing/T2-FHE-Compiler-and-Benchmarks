#ifndef HELPER_HPP_
#define HELPER_HPP_

#include <tfhe/tfhe.h>
#include <tfhe/tfhe_io.h>

// Check if n is a power of 2.
bool is_pow_of_2(int n);

// Encypt a number with the cloud key. Return result = Enc(ptxt_val).
LweSample* enc_cloud(uint32_t ptxt_val, uint32_t word_sz,
                     const TFheGateBootstrappingCloudKeySet* bk);

// Equality comparator: result = (a == b).
LweSample* cmp(const LweSample* a, const LweSample* b, const uint32_t word_sz,
               const TFheGateBootstrappingCloudKeySet* bk);

#endif  // HELPER_HPP_
