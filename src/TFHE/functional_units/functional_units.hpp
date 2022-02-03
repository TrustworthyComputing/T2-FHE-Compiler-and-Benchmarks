#ifndef TFHE_FUNCTIONAL_UNITS_HPP_
#define TFHE_FUNCTIONAL_UNITS_HPP_

#include <iostream>
#include <cassert>
#include <chrono>
#include <vector>
#include <algorithm>

#include <tfhe/tfhe.h>
#include <tfhe/tfhe_io.h>

typedef enum rotation_t { LEFT = 0, RIGHT } rotation_t;

/// CLIENT FUNCTIONS

/// Encrypt a number (or vec) with the secret key. Return result = Enc(ptxt_val).
std::vector<LweSample*> e_client(std::vector<uint32_t>& ptxt_val, size_t word_sz,
                    const TFheGateBootstrappingSecretKeySet* sk);

std::vector<LweSample*> e_client(uint32_t ptxt_val, size_t word_sz,
                    const TFheGateBootstrappingSecretKeySet* sk);

/// Decrypt a ciphertext with the secret key. Return result = Dec(ctxt)
std::vector<uint32_t> d_client(size_t word_sz, const std::vector<LweSample*>& ctxt,
                  const TFheGateBootstrappingSecretKeySet* sk);

/// Create a deep copy of a ciphertext vector. dst_ = a_
void copy(std::vector<LweSample*>& dst_, std::vector<LweSample*>& a_, 
            size_t word_sz, const TFheGateBootstrappingCloudKeySet* bk);

/// MISCELLANEOUS

/// Encode a number with the cloud key. Return result = Encode(ptxt_val).
std::vector<LweSample*> e_cloud(std::vector<uint32_t>& ptxt_val, size_t word_sz,
                    const TFheGateBootstrappingCloudKeySet* bk);

std::vector<LweSample*> e_cloud(uint32_t ptxt_val, size_t word_sz,
                   const TFheGateBootstrappingCloudKeySet* bk);

/// ARITHMETIC CIRCUITS

/// Adder circuit: result = a + b.
void add(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
         const std::vector<LweSample*>& b, const size_t nb_bits,
         const TFheGateBootstrappingCloudKeySet* bk);

/// Subtracter circuit: result = a - b.
void sub(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
         const std::vector<LweSample*>& b, const int nb_bits,
         const TFheGateBootstrappingCloudKeySet* bk);

/// Multiplier circuit: result = a * b.
void mult(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
          const std::vector<LweSample*>& b, const size_t nb_bits,
          const TFheGateBootstrappingCloudKeySet* bk);

/// Incrementer circuit: result = a + 1.
void inc(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
         const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk);

/// Incrementer circuit: result++.
void inc_inplace(std::vector<LweSample*>& result, const size_t nb_bits,
                 const TFheGateBootstrappingCloudKeySet* bk);

/// COMPARISONS

/// Equality circuit: result = (a == b).
void eq(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
        const std::vector<LweSample*>& b, const size_t word_sz,
        const TFheGateBootstrappingCloudKeySet* bk);

void neq(std::vector<LweSample*>& result_, const std::vector<LweSample*>& a,
        const std::vector<LweSample*>& b, const size_t word_sz,
        const TFheGateBootstrappingCloudKeySet* bk);

/// Less than circuit: result = (a < b)
void lt(std::vector<LweSample*>& result_, const std::vector<LweSample*>& a,
        const std::vector<LweSample*>& b, const size_t word_sz,
        const TFheGateBootstrappingCloudKeySet* bk);

void leq(std::vector<LweSample*>& result_, const std::vector<LweSample*>& a,
        const std::vector<LweSample*>& b, const size_t word_sz,
        const TFheGateBootstrappingCloudKeySet* bk);

/// BITWISE

void shift_left_bin(std::vector<LweSample*>& result, 
                    std::vector<LweSample*>& ct,
                    int amt, const size_t word_sz,
                    const TFheGateBootstrappingCloudKeySet* bk);

void shift_right_bin(std::vector<LweSample*>& result, 
                     std::vector<LweSample*>& ct,
                     int amt, const size_t word_sz,
                     const TFheGateBootstrappingCloudKeySet* bk);

void shift_right_logical_bin(std::vector<LweSample*>& result,
                     std::vector<LweSample*>& ct,
                     int amt, const size_t word_sz,
                     const TFheGateBootstrappingCloudKeySet* bk);

void e_not(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
           const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk);
           
void e_xor(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
           const std::vector<LweSample*>& b, const size_t nb_bits,
           const TFheGateBootstrappingCloudKeySet* bk);

void e_mux(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
           const std::vector<LweSample*>& b, const std::vector<LweSample*>& c,
           const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk);

#endif  // HELPER_HPP_
