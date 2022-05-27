#ifndef FUNCTIONAL_UNITS_HPP_
#define FUNCTIONAL_UNITS_HPP_

#include "seal/seal.h"
#include <algorithm>
#include <chrono>
#include <cstddef>
#include <fstream>
#include <iomanip>
#include <iostream>
#include <limits>
#include <memory>
#include <mutex>
#include <math.h>
#include <numeric>
#include <random>
#include <sstream>
#include <string>
#include <thread>
#include <vector>
#include <cassert>

std::vector<uint64_t> decrypt_array_batch_to_nums(
    seal::Decryptor& decryptor, seal::BatchEncoder& batch_encoder,
    seal::Ciphertext& encrypted_vec, size_t slots, size_t padding = 1);

seal::Ciphertext exor(seal::Ciphertext& ctxt_1, seal::Ciphertext& ctxt_2,
                           seal::Evaluator& evaluator,
                           seal::Encryptor& encryptor,
                           const seal::RelinKeys& relinKeys);

seal::Ciphertext eor(seal::Ciphertext& ctxt_1, seal::Ciphertext& ctxt_2,
                           seal::Evaluator& evaluator,
                           seal::Encryptor& encryptor,
                           const seal::RelinKeys& relinKeys);

seal::Ciphertext eand(seal::Ciphertext& ctxt_1, seal::Ciphertext& ctxt_2,
                           seal::Evaluator& evaluator,
                           seal::Encryptor& encryptor,
                           const seal::RelinKeys& relinKeys);


seal::Ciphertext mux(seal::Evaluator& evaluator, 
                     seal::RelinKeys& relin_keys, seal::Ciphertext& sel, 
                     seal::Ciphertext& ctxt_1,  seal::Ciphertext& ctxt_2,
                     size_t slots);

seal::Ciphertext eq(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext& ct1_, seal::Ciphertext& ct2_,
    size_t ptxt_mod);

seal::Ciphertext neq(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext& c1, seal::Ciphertext& c2,
    size_t ptxt_mod);

seal::Ciphertext lt(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext& ct1_, seal::Ciphertext& ct2_,
    size_t ptxt_mod);

seal::Ciphertext leq(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator, seal::RelinKeys& relin_keys,
    seal::Ciphertext& ct1_, seal::Ciphertext& ct2_, size_t ptxt_mod);

seal::Ciphertext lt_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator, seal::RelinKeys& relin_keys,
    seal::Ciphertext& ct1_, seal::Plaintext& pt1, size_t ptxt_mod);

seal::Ciphertext eq_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator, seal::RelinKeys& relin_keys,
    seal::Ciphertext& ct1_, seal::Plaintext& pt1, size_t ptxt_mod);

seal::Ciphertext neq_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext& c1, seal::Plaintext& pt1,
    size_t ptxt_mod);
    
seal::Ciphertext leq_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator, seal::RelinKeys& relin_keys,
    seal::Ciphertext& ct1_, seal::Plaintext& pt1, size_t ptxt_mod);

seal::Plaintext encode_all_slots(
    seal::BatchEncoder& batch_encoder,
    uint64_t num, size_t slots);

/// Vertical Batching functions

std::vector<seal::Ciphertext> not_bin(seal::Evaluator& evaluator, 
                            seal::BatchEncoder& batch_encoder, 
                            std::vector<seal::Ciphertext>& ct_, size_t slots);
                            
std::vector<seal::Ciphertext> shift_right_logical_bin(seal::Encryptor& encryptor,
                                              seal::BatchEncoder& batch_encoder,
                                              std::vector<seal::Ciphertext>& ct,
                                              size_t amt, size_t slots);
                                              
std::vector<seal::Ciphertext> shift_right_bin(std::vector<seal::Ciphertext>& ct,
                                              size_t amt, size_t slots);

std::vector<seal::Ciphertext> shift_left_bin(seal::Encryptor& encryptor, 
                                             seal::BatchEncoder& batch_encoder,
                                             std::vector<seal::Ciphertext>& ct, 
                                             size_t amt, size_t slots);


std::vector<seal::Ciphertext> xor_bin(seal::Evaluator& evaluator,
                                      seal::Encryptor& encryptor,
                                      seal::RelinKeys& relinKeys, 
                                      std::vector<seal::Ciphertext>& ctxt_1, 
                                      std::vector<seal::Ciphertext>& ctxt_2,
                                      size_t ptxt_mod);

std::vector<seal::Ciphertext> and_bin(seal::Evaluator& evaluator,
                                      seal::Encryptor& encryptor,
                                      seal::RelinKeys& relinKeys, 
                                      std::vector<seal::Ciphertext>& ctxt_1, 
                                      std::vector<seal::Ciphertext>& ctxt_2,
                                      size_t ptxt_mod);

std::vector<seal::Ciphertext> or_bin(seal::Evaluator& evaluator,
                                      seal::Encryptor& encryptor,
                                      seal::RelinKeys& relinKeys, 
                                      std::vector<seal::Ciphertext>& ctxt_1, 
                                      std::vector<seal::Ciphertext>& ctxt_2,
                                      size_t ptxt_mod);

std::vector<seal::Ciphertext> mux_bin(seal::Evaluator& evaluator,
                                      seal::BatchEncoder& batch_encoder,
                                      seal::RelinKeys& relin_keys, 
                                      std::vector<seal::Ciphertext>& sel, 
                                      std::vector<seal::Ciphertext>& ctxt_1, 
                                      std::vector<seal::Ciphertext>& ctxt_2,
                                      size_t slots);
std::vector<seal::Ciphertext> eq_bin(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, seal::RelinKeys& relin_keys, 
    std::vector<seal::Ciphertext>& ct1_, std::vector<seal::Ciphertext>& ct2_, 
    size_t word_sz, size_t slots);

std::vector<seal::Ciphertext> neq_bin(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, seal::RelinKeys& relin_keys, 
    std::vector<seal::Ciphertext>& c1, std::vector<seal::Ciphertext>& c2, 
    size_t word_sz, size_t slots);

std::vector<seal::Ciphertext> lt_bin(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, seal::RelinKeys& relin_keys, 
    std::vector<seal::Ciphertext>& ct1_, std::vector<seal::Ciphertext>& ct2_, 
    size_t word_sz, size_t slots);

std::vector<seal::Ciphertext> leq_bin(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, seal::RelinKeys& relin_keys, 
    std::vector<seal::Ciphertext>& ct1_, std::vector<seal::Ciphertext>& ct2_, 
    size_t word_sz, size_t slots);

std::vector<seal::Ciphertext> sub_bin(seal::Evaluator& evaluator, 
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder, 
    seal::RelinKeys& relin_keys, std::vector<seal::Ciphertext>& ct1_, 
    std::vector<seal::Ciphertext>& ct2_, size_t slots);

std::vector<seal::Ciphertext> inc_bin(seal::Evaluator& evaluator, 
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder, 
    seal::RelinKeys& relin_keys, std::vector<seal::Ciphertext>& ct1_, 
    size_t slots);
  
std::vector<seal::Ciphertext> dec_bin(seal::Evaluator& evaluator, 
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder, 
    seal::RelinKeys& relin_keys, std::vector<seal::Ciphertext>& ct1_, 
    size_t slots);
    
std::vector<seal::Ciphertext> add_bin(seal::Evaluator& evaluator, 
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder, 
    seal::RelinKeys& relin_keys, std::vector<seal::Ciphertext>& ct1_, 
    std::vector<seal::Ciphertext>& ct2_, size_t slots);

std::vector<seal::Ciphertext> mult_bin(seal::Evaluator& evaluator, 
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder, 
    seal::RelinKeys& relin_keys, std::vector<seal::Ciphertext>& ct1_, 
    std::vector<seal::Ciphertext>& ct2_, size_t slots);
    
/// Helper function: Prints the `parms_id' to std::ostream.
inline std::ostream &operator<<(std::ostream &stream, seal::parms_id_type parms_id) {
  // Save the formatting information for std::cout.
  std::ios old_fmt(nullptr);
  old_fmt.copyfmt(std::cout);

  stream << std::hex << std::setfill('0') << std::setw(16) << parms_id[0]
         << " " << std::setw(16) << parms_id[1] << " " << std::setw(16)
         << parms_id[2] << " " << std::setw(16) << parms_id[3] << " ";

  // Restore the old std::cout formatting.
  std::cout.copyfmt(old_fmt);

  return stream;
}

/// Helper function: Prints the `parms_id' to std::ostream.
inline std::ostream &operator<<(std::ostream &stream, seal::Plaintext ptxt) {
  // Save the formatting information for std::cout.
  std::ios old_fmt(nullptr);
  old_fmt.copyfmt(std::cout);

  stream << std::stol(ptxt.to_string(), nullptr, 16);

  // Restore the old std::cout formatting.
  std::cout.copyfmt(old_fmt);

  return stream;
}

/// Helper function: Convert a value into a hexadecimal string, e.g., uint64_t(17) --> "11".
inline std::string uint64_to_hex_string(std::uint64_t value) {
  return seal::util::uint_to_hex_string(&value, std::size_t(1));
}

#endif  // FUNCTIONAL_UNITS_HPP_
