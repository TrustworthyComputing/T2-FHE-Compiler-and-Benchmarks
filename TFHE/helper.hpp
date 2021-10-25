#ifndef HELPER_HPP_
#define HELPER_HPP_

#include <iostream>
#include <cassert>
#include <chrono>

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

/// Check if n is a power of 2.
bool is_pow_of_2(int n);

/// Encypt a number with the cloud key. Return result = Enc(ptxt_val).
LweSample* enc_cloud(uint32_t ptxt_val, size_t word_sz,
                     const TFheGateBootstrappingCloudKeySet* bk);

/// Adder circuit: result = a + b.
void add(LweSample* result, const LweSample* a, const LweSample* b,
         const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk);

/// Adder circuit: a += b.
void add_inplace(LweSample* a, const LweSample* b, const size_t nb_bits,
                 const TFheGateBootstrappingCloudKeySet* bk);

/// Multiplier circuit: result = a * b.
void mult(LweSample* result, const LweSample* a, const LweSample* b,
          const size_t nb_bits, const TFheGateBootstrappingCloudKeySet* bk);

/// Multiplier circuit: a *= b.
void mult_inplace(LweSample* a, const LweSample* b, const size_t nb_bits,
                  const TFheGateBootstrappingCloudKeySet* bk);

/// Incrementer circuit: result = a + 1.
void inc(LweSample* result, const LweSample* a, const size_t nb_bits,
         const TFheGateBootstrappingCloudKeySet* bk);

/// Increment ciphertext a by 1 and store result to a. a++.
void inc_inplace(LweSample* a, const size_t nb_bits,
                 const TFheGateBootstrappingCloudKeySet* bk);

/// General comparator: result = (a == b).
void cmp(LweSample* result, const LweSample* a, const LweSample* b,
         const size_t word_sz, const TFheGateBootstrappingCloudKeySet* bk);

/// Equality circuit: result = (a == b).
void eq(LweSample* result, const LweSample* a, const LweSample* b,
        const size_t word_sz, const TFheGateBootstrappingCloudKeySet* bk);

/// Comparator circuit: result = (a <= b).
void leq(LweSample* result, const LweSample* a, const LweSample* b,
         const size_t word_sz, const TFheGateBootstrappingCloudKeySet* bk);

void rotate_inplace(LweSample* result, rotation_t dir, int amt,
                    const size_t word_sz, 
                    const TFheGateBootstrappingCloudKeySet* bk);

#endif  // HELPER_HPP_
