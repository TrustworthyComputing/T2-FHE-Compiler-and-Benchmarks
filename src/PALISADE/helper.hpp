#ifndef HELPER_HPP_
#define HELPER_HPP_

#include <stdio.h>
#include <string.h>
#include <iostream>
#include <fstream>
#include <cassert>
#include <vector>

#include "palisade.h"

using namespace lbcrypto;

typedef enum scheme_t {
  BFV, BGV, CKKS, TFHE, NONE
} scheme_t;

/// Helper function to encrypt an integer repeatedly into a packed plaintext
Ciphertext<DCRTPoly> encrypt_repeated_integer(CryptoContext<DCRTPoly>& cc,
                                              LPPublicKey<DCRTPoly>& pk,
                                              int64_t num, size_t n);

#endif  // HELPER_HPP_
