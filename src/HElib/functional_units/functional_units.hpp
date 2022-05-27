#ifndef FUNCTIONAL_UNITS_HPP_
#define FUNCTIONAL_UNITS_HPP_

#include "helib/helib.h"
#include <helib/binaryArith.h>
#include <helib/PtrVector.h>
#include <helib/binaryCompare.h>
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

/// XOR between two batched binary ciphertexts
helib::Ctxt exor(helib::PubKey& public_key, helib::Ctxt& ctxt_1, 
    helib::Ctxt& ctxt_2);

helib::Ctxt eand(helib::PubKey& public_key, helib::Ctxt& ctxt_1, 
    helib::Ctxt& ctxt_2);

helib::Ctxt eor(helib::PubKey& public_key, helib::Ctxt& ctxt_1, 
    helib::Ctxt& ctxt_2);

helib::Ctxt mux(helib::PubKey& public_key, helib::Ctxt& sel, helib::Ctxt& ctxt_1,
                helib::Ctxt& ctxt_2);

std::vector<helib::Ctxt> shift_right_logical_bin(helib::PubKey& public_key,
                                        std::vector<helib::Ctxt>& ct, 
                                        size_t amt);
                                        
std::vector<helib::Ctxt> shift_right_bin(helib::PubKey& public_key,
                                         std::vector<helib::Ctxt>& ct,
                                         size_t amt);

std::vector<helib::Ctxt> shift_left_bin(helib::PubKey& public_key,
                                        std::vector<helib::Ctxt>& ct, 
                                        size_t amt);

std::vector<helib::Ctxt> not_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& ct);
                                 
std::vector<helib::Ctxt> xor_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 size_t ptxt_mod);

std::vector<helib::Ctxt> or_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 size_t ptxt_mod);

std::vector<helib::Ctxt> and_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 size_t ptxt_mod);

std::vector<helib::Ctxt> mux_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& sel, 
                                 std::vector<helib::Ctxt>& ctxt_1,
                                 std::vector<helib::Ctxt>& ctxt_2);

std::vector<helib::Ctxt> add_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots);

std::vector<helib::Ctxt> sub_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots);

std::vector<helib::Ctxt> mult_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots);

std::vector<helib::Ctxt> eq_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots);

std::vector<helib::Ctxt> neq_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots);

std::vector<helib::Ctxt> lt_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots);

std::vector<helib::Ctxt> leq_bin(helib::PubKey& public_key, 
                                 std::vector<helib::Ctxt>& c1, 
                                 std::vector<helib::Ctxt>& c2, 
                                 std::vector<helib::zzX>& unpackSlotEncoding, 
                                 size_t slots);
                                 
helib::Ctxt eq(helib::PubKey& public_key, helib::Ctxt& c1, helib::Ctxt& c2, 
               size_t ptxt_mod, size_t slots);

helib::Ctxt neq(helib::PubKey& public_key, helib::Ctxt& c1, helib::Ctxt& c2, 
                size_t ptxt_mod, size_t slots);

helib::Ctxt lt(helib::PubKey& public_key, helib::Ctxt& c1, helib::Ctxt& c2, 
               size_t ptxt_mod, size_t slots);

helib::Ctxt leq(helib::PubKey& public_key, helib::Ctxt& c1, helib::Ctxt& c2, 
                size_t ptxt_mod, size_t slots);
    
helib::Ctxt lt_plain(
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ptxt<helib::BGV>& pt1, 
    size_t ptxt_mod, size_t slots);

helib::Ctxt lt_plain(
    helib::PubKey& public_key, helib::Ptxt<helib::BGV>& pt1, helib::Ctxt& c1,
    size_t ptxt_mod, size_t slots);

helib::Ctxt eq_plain(
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ptxt<helib::BGV>& pt1, 
    size_t ptxt_mod, size_t slots);

helib::Ctxt neq_plain(
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ptxt<helib::BGV>& pt1, 
    size_t ptxt_mod, size_t slots);
    
helib::Ctxt leq_plain(
    helib::PubKey& public_key, helib::Ctxt& c1, helib::Ptxt<helib::BGV>& pt1, 
    size_t ptxt_mod, size_t slots);

helib::Ctxt leq_plain(
    helib::PubKey& public_key, helib::Ptxt<helib::BGV>& pt1, helib::Ctxt& c1, 
    size_t ptxt_mod, size_t slots);

#endif  // FUNCTIONAL_UNITS_HPP_
