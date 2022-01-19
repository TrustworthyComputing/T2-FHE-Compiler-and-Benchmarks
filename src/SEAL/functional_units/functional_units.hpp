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

/// Encrypt/Decrypt an integer to a vector of encryptions of zero and one
std::vector<seal::Ciphertext> encrypt_num_to_binary_array(
    seal::Encryptor& encryptor, uint64_t number, size_t word_sz);

uint64_t decrypt_binary_array(
    seal::Decryptor& decryptor, std::vector<seal::Ciphertext>& encrypted_vec);

/// Encrypt/Decrypt an integer to a binary vector batched
seal::Ciphertext encrypt_num_to_binary_array_batch(
    seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder,
    uint64_t number, size_t word_sz, size_t slots, size_t padding = 1);

uint64_t decrypt_binary_array_batch(
    seal::Decryptor& decryptor, seal::BatchEncoder& batch_encoder,
    seal::Ciphertext& encrypted_vec, size_t word_sz, size_t padding = 1);

/// Encrypt/Decrypt a vector of integers to a batched ciphertext
seal::Ciphertext encrypt_nums_to_array_batch(seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, std::vector<uint64_t>& nums,
    size_t num_elems, size_t slots, size_t padding = 1);

std::vector<uint64_t> decrypt_array_batch_to_nums(
    seal::Decryptor& decryptor, seal::BatchEncoder& batch_encoder,
    seal::Ciphertext& encrypted_vec, size_t slots, size_t padding = 1);

/// XOR between two batched binary ciphertexts
seal::Ciphertext xor_batch(seal::Ciphertext& ctxt_1, seal::Ciphertext& ctxt_2,
                           seal::Evaluator& evaluator,
                           const seal::RelinKeys& relinKeys);

seal::Ciphertext eq_bin_batched(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext& ct1_, seal::Ciphertext& ct2_,
    seal::GaloisKeys& galios_keys, size_t slots, size_t padding);

seal::Ciphertext lt_bin_batched(
    seal::Evaluator& evaluator, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext& ct1_, seal::Ciphertext& ct2_,
    size_t slots);

seal::Ciphertext lt_bin_batched_plain(
    seal::Evaluator& evaluator, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext& ct1_, seal::Plaintext& pt1_,
    size_t slots);

seal::Ciphertext lte_bin_batched_plain(
    seal::Evaluator& evaluator, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext& ct1_, seal::Plaintext& pt1_,
    size_t slots);

seal::Ciphertext lte_bin_batched(
    seal::Evaluator& evaluator, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext& ct1_, seal::Plaintext& pt1_,
    size_t slots);

seal::Ciphertext eq_bin_batched_plain(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor, seal::BatchEncoder& batch_encoder,
    seal::RelinKeys& relin_keys, seal::Ciphertext& ct1_, seal::Plaintext& pt1_,
    seal::GaloisKeys& galios_keys, size_t slots, size_t padding);

seal::Ciphertext eq(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator,
    seal::RelinKeys& relin_keys, seal::Ciphertext& ct1_, seal::Ciphertext& ct2_,
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
    seal::Ciphertext& ct1_, seal::Plaintext& pt1_, size_t ptxt_mod);

seal::Ciphertext eq_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator, seal::RelinKeys& relin_keys,
    seal::Ciphertext& ct1_, seal::Plaintext& pt1_, size_t ptxt_mod);

seal::Ciphertext leq_plain(
    seal::Encryptor& encryptor, seal::Evaluator& evaluator, seal::RelinKeys& relin_keys,
    seal::Ciphertext& ct1_, seal::Plaintext& pt1_, size_t ptxt_mod);

seal::Plaintext encode_all_slots(
    seal::BatchEncoder& batch_encoder,
    uint64_t num, size_t slots);

/// Vertical Batching functions

std::vector<seal::Ciphertext> eq_bin(
    seal::Evaluator& evaluator, seal::Encryptor& encryptor,
    seal::BatchEncoder& batch_encoder, seal::RelinKeys& relin_keys, 
    std::vector<seal::Ciphertext>& ct1_, std::vector<seal::Ciphertext>& ct2_, 
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
    
/// Helper function: Prints the name of the example in a fancy banner.
inline void print_example_banner(std::string title) {
  if (!title.empty()) {
    std::size_t title_length = title.length();
    std::size_t banner_length = title_length + 2 * 10;
    std::string banner_top = "+" + std::string(banner_length - 2, '-') + "+";
    std::string banner_middle = "|" + std::string(9, ' ') + title + std::string(9, ' ') + "|";

    std::cout << std::endl << banner_top << std::endl << banner_middle
              << std::endl << banner_top << std::endl;
  }
}

/// Helper function: Prints the parameters in a SEALContext.
inline void print_parameters(const seal::SEALContext &context) {
  auto &context_data = *context.key_context_data();
  // Which scheme are we using?
  std::string scheme_name;
  switch (context_data.parms().scheme()) {
    case seal::scheme_type::bfv:
      scheme_name = "BFV";
      break;
    case seal::scheme_type::ckks:
      scheme_name = "CKKS";
      break;
    default:
      throw std::invalid_argument("unsupported scheme");
  }
  std::cout << "/" << std::endl;
  std::cout << "| Encryption parameters :" << std::endl;
  std::cout << "|   scheme: " << scheme_name << std::endl;
  std::cout << "|   poly_modulus_degree: "
            << context_data.parms().poly_modulus_degree() << std::endl;

  // Print the size of the true (product) coefficient modulus.
  std::cout << "|   coeff_modulus size: ";
  std::cout << context_data.total_coeff_modulus_bit_count() << " (";
  auto coeff_modulus = context_data.parms().coeff_modulus();
  std::size_t coeff_modulus_size = coeff_modulus.size();
  for (std::size_t i = 0; i < coeff_modulus_size - 1; i++) {
    std::cout << coeff_modulus[i].bit_count() << " + ";
  }
  std::cout << coeff_modulus.back().bit_count();
  std::cout << ") bits" << std::endl;

  // For the BFV scheme print the plain_modulus parameter.
  if (context_data.parms().scheme() == seal::scheme_type::bfv) {
    std::cout << "|   plain_modulus: "
              << context_data.parms().plain_modulus().value() << std::endl;
  }

  std::cout << "\\" << std::endl;
}

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

/// Helper function: Prints a vector of floating-point values.
template <typename T>
inline void print_vector(std::vector<T> vec, std::size_t print_size = 4, int prec = 3) {
  // Save the formatting information for std::cout.
  std::ios old_fmt(nullptr);
  old_fmt.copyfmt(std::cout);

  std::size_t slot_count = vec.size();

  std::cout << std::fixed << std::setprecision(prec);
  std::cout << std::endl;
  if (slot_count <= 2 * print_size) {
    std::cout << "    [";
    for (std::size_t i = 0; i < slot_count; i++) {
      std::cout << " " << vec[i] << ((i != slot_count - 1) ? "," : " ]\n");
    }
  } else {
    vec.resize(std::max(vec.size(), 2 * print_size));
    std::cout << "    [";
    for (std::size_t i = 0; i < print_size; i++) {
      std::cout << " " << vec[i] << ",";
    }
    if (vec.size() > 2 * print_size) {
      std::cout << " ...,";
    }
    for (std::size_t i = slot_count - print_size; i < slot_count; i++) {
      std::cout << " " << vec[i] << ((i != slot_count - 1) ? "," : " ]\n");
    }
  }
  std::cout << std::endl;

  // Restore the old std::cout formatting.
  std::cout.copyfmt(old_fmt);
}

/// Helper function: Prints a matrix of values.
template <typename T>
inline void print_matrix(std::vector<T> matrix, std::size_t row_size) {
  // We're not going to print every column of the matrix (there are 2048). Instead
  // print this many slots from beginning and end of the matrix.
  std::size_t print_size = 5;

  std::cout << std::endl;
  std::cout << "    [";
  for (std::size_t i = 0; i < print_size; i++) {
    std::cout << std::setw(3) << std::right << matrix[i] << ",";
  }
  std::cout << std::setw(3) << " ...,";
  for (std::size_t i = row_size - print_size; i < row_size; i++) {
    std::cout << std::setw(3) << matrix[i] << ((i != row_size - 1) ? "," : " ]\n");
  }
  std::cout << "    [";
  for (std::size_t i = row_size; i < row_size + print_size; i++) {
    std::cout << std::setw(3) << matrix[i] << ",";
  }
  std::cout << std::setw(3) << " ...,";
  for (std::size_t i = 2 * row_size - print_size; i < 2 * row_size; i++) {
    std::cout << std::setw(3) << matrix[i] << ((i != 2 * row_size - 1) ? "," : " ]\n");
  }
  std::cout << std::endl;
}

/// Helper function: Print line number.
inline void print_line(int line_number) {
  std::cout << "Line " << std::setw(3) << line_number << " --> ";
}

/// Helper function: Convert a value into a hexadecimal string, e.g., uint64_t(17) --> "11".
inline std::string uint64_to_hex_string(std::uint64_t value) {
  return seal::util::uint_to_hex_string(&value, std::size_t(1));
}

#endif  // FUNCTIONAL_UNITS_HPP_
