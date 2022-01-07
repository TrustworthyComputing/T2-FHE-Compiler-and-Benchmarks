#ifndef FUNCTIONAL_UNITS_HPP_
#define FUNCTIONAL_UNITS_HPP_

#include "helib/helib.h"
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
std::vector<helib::Ctxt> encrypt_num_to_binary_array(
    helib::PubKey& public_key, helib::Context& context, uint64_t number, 
    size_t word_sz);

uint64_t decrypt_binary_array(
    helib::SecKey& secret_key, helib::Context& context, 
    std::vector<helib::Ctxt>& encrypted_vec);

/// Encrypt/Decrypt an integer to a binary vector batched
helib::Ctxt encrypt_num_to_binary_array_batch(
    helib::PubKey& public_key, helib::Context& context, uint64_t number, 
    size_t word_sz, size_t slots, size_t padding = 1);

uint64_t decrypt_binary_array_batch(
    helib::SecKey& secret_key, helib::Context& context, 
    helib::Ctxt encrypted_vec, size_t word_sz, size_t padding = 1);

/// Encrypt/Decrypt a vector of integers to a batched ciphertext
helib::Ctxt encrypt_nums_to_array_batch(helib::PubKey& public_key, 
    helib::Context& context, std::vector<uint64_t> nums,  size_t num_elems, 
    size_t slots, size_t padding = 1);

std::vector<uint64_t> decrypt_array_batch_to_nums( helib::SecKey& secret_key, 
    helib::Context& context, helib::Ctxt encrypted_vec, size_t slots, 
    size_t padding = 1);

/// XOR between two batched binary ciphertexts
helib::Ctxt xor_batch(helib::PubKey& public_key, helib::Ctxt& ctxt_1, 
    helib::Ctxt& ctxt_2);

helib::Ctxt eq_bin_batched(helib::PubKey& public_key, helib::Context& context, 
    helib::Ctxt ct1_, helib::Ctxt ct2_, size_t slots, const helib::EncryptedArray& ea,
    size_t padding);

helib::Ctxt lt_bin_batched(helib::PubKey& public_key, helib::Ctxt ct1_, 
    helib::Ctxt ct2_, size_t slots);
    
helib::Ctxt lt_bin_batched_plain(helib::PubKey& public_key, helib::Ctxt ct1_, 
    helib::Ptxt<helib::BGV> pt1_, size_t slots);

helib::Ctxt lte_bin_batched_plain(helib::PubKey& public_key, helib::Ctxt ct1_, 
    helib::Ptxt<helib::BGV> pt1_, size_t slots);
    
helib::Ctxt lte_bin_batched(helib::PubKey& public_key, helib::Ctxt ct1_, 
    helib::Ctxt ct2_, size_t slots);

helib::Ctxt eq_bin_batched_plain(helib::PubKey& public_key, 
    helib::Context& context, helib::Ctxt ct1_, helib::Ptxt<helib::BGV> pt1_, 
    helib::EncryptedArray& ea, size_t slots, size_t padding);

helib::Ctxt eq(
    helib::PubKey& public_key, helib::Ctxt ct1_, helib::Ctxt ct2_, 
    size_t ptxt_mod, size_t slots);

helib::Ctxt lt(
    helib::PubKey& public_key, helib::Ctxt ct1_, helib::Ctxt ct2_, 
    size_t ptxt_mod, size_t slots);

helib::Ctxt leq(
    helib::PubKey& public_key, helib::Ctxt ct1_, helib::Ctxt ct2_, size_t ptxt_mod, 
    size_t slots);
    
helib::Ctxt lt_plain(
    helib::PubKey& public_key, helib::Ctxt ct1_, helib::Ptxt<helib::BGV> pt1_, 
    size_t ptxt_mod, size_t slots);

helib::Ctxt lt_plain(
    helib::PubKey& public_key, helib::Ptxt<helib::BGV> pt1_, helib::Ctxt ct1_,
    size_t ptxt_mod, size_t slots);

helib::Ctxt eq_plain(
    helib::PubKey& public_key, helib::Ctxt ct1_, helib::Ptxt<helib::BGV> pt1_, 
    size_t ptxt_mod, size_t slots);

helib::Ctxt leq_plain(
    helib::PubKey& public_key, helib::Ctxt ct1_, helib::Ptxt<helib::BGV> pt1_, 
    size_t ptxt_mod, size_t slots);

helib::Ctxt leq_plain(
    helib::PubKey& public_key, helib::Ptxt<helib::BGV> pt1_, helib::Ctxt ct1_, 
    size_t ptxt_mod, size_t slots);

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

#endif  // FUNCTIONAL_UNITS_HPP_
