#ifndef TFHE_FUNCTIONAL_UNITS_HPP_
#define TFHE_FUNCTIONAL_UNITS_HPP_

#include <iostream>
#include <cassert>
#include <chrono>
#include <vector>
#include <algorithm>

#include <tfhe/tfhe.h>
#include <tfhe/tfhe_io.h>

#define duration(a) \
  std::chrono::duration_cast<std::chrono::milliseconds>(a).count()
#define duration_ns(a) \
  std::chrono::duration_cast<std::chrono::nanoseconds>(a).count()
#define duration_us(a) \
  std::chrono::duration_cast<std::chrono::microseconds>(a).count()
#define duration_ms(a) \
  std::chrono::duration_cast<std::chrono::milliseconds>(a).count()
#define timeNow() std::chrono::high_resolution_clock::now()

#define TIC(t) t = timeNow()
#define TOC(t) duration(timeNow() - t)
#define TOC_NS(t) duration_ns(timeNow() - t)
#define TOC_US(t) duration_us(timeNow() - t)
#define TOC_MS(t) duration_ms(timeNow() - t)

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

/// Rotate ciphertext array to the left or right by amt.
void rotate_inplace(std::vector<LweSample*>& result, rotation_t dir, int amt,
                    const size_t word_sz,
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

/// Less than circuit: result = (a < b)
void lt(std::vector<LweSample*>& result_, const std::vector<LweSample*>& a,
        const std::vector<LweSample*>& b, const size_t word_sz,
        const TFheGateBootstrappingCloudKeySet* bk);

void leq(std::vector<LweSample*>& result_, const std::vector<LweSample*>& a,
        const std::vector<LweSample*>& b, const size_t word_sz,
        const TFheGateBootstrappingCloudKeySet* bk);

/// BITWISE

void e_not(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
           const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk);

void e_and(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
           const std::vector<LweSample*>& b, const size_t nb_bits,
           const TFheGateBootstrappingCloudKeySet* bk);

void e_or(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
          const std::vector<LweSample*>& b, const size_t nb_bits,
          const TFheGateBootstrappingCloudKeySet* bk);

void e_nand(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
            const std::vector<LweSample*>& b, const size_t nb_bits,
            const TFheGateBootstrappingCloudKeySet* bk);

void e_nor(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
           const std::vector<LweSample*>& b, const size_t nb_bits,
           const TFheGateBootstrappingCloudKeySet* bk);

void e_xor(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
           const std::vector<LweSample*>& b, const size_t nb_bits,
           const TFheGateBootstrappingCloudKeySet* bk);

void e_xnor(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
            const std::vector<LweSample*>& b, const size_t nb_bits,
            const TFheGateBootstrappingCloudKeySet* bk);

void e_mux(std::vector<LweSample*>& result, const std::vector<LweSample*>& a,
           const std::vector<LweSample*>& b, const std::vector<LweSample*>& c,
           const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk);

/// INTEGER DOMAIN

// /// Encrypt integer as a single ciphertext.
// std::vector<LweSample*> e_client_int(uint32_t ptxt_val, uint32_t ptxt_mod,
//                         const TFheGateBootstrappingSecretKeySet* sk);
//
// /// Decrypt an integer ciphertext with the secret key. Return result = Dec(ctxt)
// std::vector<uint32_t> d_client_int(uint32_t ptxt_mod,
//                                    const std::vector<LweSample*> ctxt,
//                                    const TFheGateBootstrappingSecretKeySet* sk);
//
// /// Encode integer as a single noiseless ciphertext.
// std::vector<LweSample*> e_cloud_int(int32_t ptxt_val, uint32_t ptxt_mod,
//                        const TFheGateBootstrappingCloudKeySet* bk);
//
// std::vector<LweSample*> e_cloud_int(std::vector<int32_t> ptxt_val,
//                                     uint32_t ptxt_mod,
//                                     const TFheGateBootstrappingCloudKeySet* bk);
//
// /// Convert {0,1} % 2 to {-1,1} % ptxt_mod.
// std::vector<LweSample*> e_bin_to_int(std::vector<LweSample*> a, uint32_t ptxt_mod,
//                                      const TFheGateBootstrappingCloudKeySet* bk);
//
// /// Convert {0, p-1} % p to {0,1} % 2 (sign extraction)
// std::vector<LweSample*> e_int_to_bin(std::vector<LweSample*> a,
//                                      const TFheGateBootstrappingCloudKeySet* bk);
//
// /// Add operation: result = a + b.
// void add_int(std::vector<LweSample*> result, const std::vector<LweSample*> a,
//              const std::vector<LweSample*> b,
//              const TFheGateBootstrappingCloudKeySet* bk);
//
// /// Sub operation: result = a - b.
// void sub_int(std::vector<LweSample*> result, const std::vector<LweSample*> a,
//              const std::vector<LweSample*> b,
//              const TFheGateBootstrappingCloudKeySet* bk);
//
// /// Scalar multiplication: result = a * p.
// void mult_plain_int(std::vector<LweSample*> result, const std::vector<LweSample*> a,
//                     int32_t p, const TFheGateBootstrappingCloudKeySet* bk);

#endif  // HELPER_HPP_
