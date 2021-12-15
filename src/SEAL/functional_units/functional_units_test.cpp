#include "functional_units.hpp"
#include <iostream>

using namespace std;
using namespace seal;

int main(void) {
  size_t poly_modulus_degree = 16384;
  EncryptionParameters parms(scheme_type::bfv);
  parms.set_poly_modulus_degree(poly_modulus_degree);
  parms.set_coeff_modulus(CoeffModulus::BFVDefault(poly_modulus_degree));
  parms.set_plain_modulus(PlainModulus::Batching(poly_modulus_degree, 20));
  uint64_t plaintext_modulus = parms.plain_modulus().value();
  SEALContext context(parms);
  KeyGenerator keygen(context);
  SecretKey secret_key = keygen.secret_key();
  PublicKey public_key;
  RelinKeys relin_keys;
  GaloisKeys galois_keys;
  keygen.create_public_key(public_key);
  keygen.create_relin_keys(relin_keys);
  keygen.create_galois_keys(galois_keys);
  Encryptor encryptor(context, public_key);
  Evaluator evaluator(context);
  Decryptor decryptor(context, secret_key);
  BatchEncoder batch_encoder(context);
  std::vector<uint64_t> pt1 = {1, 2, 3, 4, 5};

  // Batched Enc/Dec
  Ciphertext ct1_ = encrypt_nums_to_array_batch(encryptor, batch_encoder, pt1,
                                          pt1.size(), poly_modulus_degree/2, 1);
  std::vector<uint64_t> pt1_rec = decrypt_array_batch_to_nums(decryptor, batch_encoder,
                                                        ct1_, poly_modulus_degree/2, 1);
  for (int i = 0; i < pt1.size(); i++) { assert(pt1[i] == pt1_rec[i]); }

  // Batched Binary Equality (Ctxt-Ctxt)
  pt1 = {1, 0, 1, 0, 1};
  ct1_ = encrypt_nums_to_array_batch(encryptor, batch_encoder, pt1, pt1.size(),
                                     poly_modulus_degree/2, 1);
  std::vector<uint64_t> pt2 = {0, 0, 1, 1, 1};
  Ciphertext ct2_ = encrypt_nums_to_array_batch(encryptor, batch_encoder, pt2,
                                          pt2.size(), poly_modulus_degree/2, 1);
  ct1_ = eq_bin_batched(evaluator, encryptor, batch_encoder, relin_keys, ct1_, ct2_,
                        galois_keys, poly_modulus_degree/2, 1);
  pt1_rec = decrypt_array_batch_to_nums(decryptor, batch_encoder, ct1_,
                                        poly_modulus_degree/2, 1);
  for (int i = 0; i < pt1.size(); i++) {
    pt1[i] = (uint64_t)(pt1[i] == pt2[i]);
    assert(pt1_rec[i] == pt1[i]);
  }

  // Batched Binary Equality (Ctxt-Ptxt)
  pt1 = {1, 0, 1, 0, 1};
  ct1_ = encrypt_nums_to_array_batch(encryptor, batch_encoder, pt1, pt1.size(),
                                     poly_modulus_degree/2, 1);
  pt2 = {0, 0, 1, 1, 1};
  seal::Plaintext const1;
  batch_encoder.encode(pt2, const1);
  ct1_ = eq_bin_batched_plain(evaluator, encryptor, batch_encoder, relin_keys, ct1_, const1,
                        galois_keys, poly_modulus_degree/2, 1);
  pt1_rec = decrypt_array_batch_to_nums(decryptor, batch_encoder, ct1_,
                                        poly_modulus_degree/2, 1);
  for (int i = 0; i < pt1.size(); i++) {
    pt1[i] = (uint64_t)(pt1[i] == pt2[i]);
    assert(pt1_rec[i] == pt1[i]);
  }

  // Batched Binary Less-Than (Ctxt-Ctxt)
  pt1 = {1, 0, 1, 0, 1};
  ct1_ = encrypt_nums_to_array_batch(encryptor, batch_encoder, pt1, pt1.size(),
                                     poly_modulus_degree/2, 1);
  pt2 = {0, 0, 1, 1, 1};
  ct2_ = encrypt_nums_to_array_batch(encryptor, batch_encoder, pt2, pt2.size(),
                                     poly_modulus_degree/2, 1);
  ct1_ = lt_bin_batched(evaluator, batch_encoder, relin_keys, ct1_, ct2_,
                        poly_modulus_degree/2);
  pt1_rec = decrypt_array_batch_to_nums(decryptor, batch_encoder, ct1_,
                                        poly_modulus_degree/2, 1);
  for (int i = 0; i < pt1.size(); i++) {
    pt1[i] = (uint64_t)(pt1[i] < pt2[i]);
    assert(pt1_rec[i] == pt1[i]);
  }

  // Batched Binary Less-Than (Ctxt-Ptxt)
  pt1 = {1, 0, 1, 0, 1};
  ct1_ = encrypt_nums_to_array_batch(encryptor, batch_encoder, pt1, pt1.size(),
                                     poly_modulus_degree/2, 1);
  pt2 = {0, 0, 1, 1, 1};
  batch_encoder.encode(pt2, const1);
  ct1_ = lt_bin_batched_plain(evaluator, batch_encoder, relin_keys, ct1_, const1,
                              poly_modulus_degree/2);
  pt1_rec = decrypt_array_batch_to_nums(decryptor, batch_encoder, ct1_,
                                        poly_modulus_degree/2, 1);
  for (int i = 0; i < pt1.size(); i++) {
    pt1[i] = (uint64_t)(pt1[i] < pt2[i]);
    assert(pt1_rec[i] == pt1[i]);
  }
  cout << "All tests passed!" << endl;
}
