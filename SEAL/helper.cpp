#include "helper.hpp"

std::vector<seal::Ciphertext> encrypt_num_to_binary_array(
    seal::Encryptor& encryptor, uint64_t number, size_t word_sz) {
  // Convert integer to binary.
  std::vector<seal::Ciphertext> result(word_sz);
  for (int i = 0; i < word_sz; ++i) {
    uint64_t val = (number >> i) & 1;
    // encode bit as integer

    seal::Plaintext b(uint64_to_hex_string(val));
    // encrypt bit
    encryptor.encrypt(b, result.at(i));
  }
  return result;
}

  // seal::Plaintext p;
  // decryptor->decrypt(ctxt, p);
  // std::vector<uint64_t> dec;
  // encoder->decode(p, dec);

// seal::Ciphertext encrypt_num_to_binary_array_batch(
//     seal::Encryptor& encryptor, uint64_t number, size_t word_sz) {
//   std::vector<seal::Plaintext> ptxt_vec(word_sz);
//   // Convert integer to binary.
//   for (int i = 0; i < word_sz; ++i) {
//     uint64_t val = (number >> i) & 1;
//     ptxt_vec[i] = uint64_to_hex_string(val);
//   }
//   // Batch encrypt bits.
//   seal::Ciphertext result;
//   encryptor.encrypt(ptxt_vec, result);
//   return result;
// }

// uint64_t decrypt_binary_array_batch(seal::Decryptor& decryptor,
//                                     seal::Ciphertext ctxt) {
//   // Convert binary ciphertext to integer.
//   uint64_t result = 0;
//   seal::Plaintext tmp;
//   decryptor.decrypt(ctxt[i], tmp);
//   for (int i = 0; i < 64; ++i) {
//     uint64_t b = std::stol(tmp.to_string(), nullptr, 16);
//     // uint64_t b = tmp.data_;
//     if (b == 1) {
//       result |= ((uint64_t) 1 << i);
//     }
//   }

//   return result;
// }

uint64_t decrypt_binary_array(seal::Decryptor& decryptor,
                              std::vector<seal::Ciphertext>& ctxt) {
  // Convert binary ciphertext to integer.
  uint64_t result = 0;
  seal::Plaintext tmp;
  for (int i = 0; i < ctxt.size(); ++i) {
    decryptor.decrypt(ctxt[i], tmp);

    uint64_t b = std::stol(tmp.to_string(), nullptr, 16);
    // uint64_t b = tmp.data_;
    if (b == 1) {
      result |= ((uint64_t) 1 << i);
    }
  }

  return result;
}

