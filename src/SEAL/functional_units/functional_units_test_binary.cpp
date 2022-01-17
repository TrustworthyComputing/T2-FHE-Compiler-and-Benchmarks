#include "functional_units.hpp"
#include <iostream>

using namespace std;
using namespace seal;

int main(void) {
  size_t poly_modulus_degree = 32768;
  EncryptionParameters parms(scheme_type::bfv);
  parms.set_poly_modulus_degree(poly_modulus_degree);
  parms.set_coeff_modulus(CoeffModulus::BFVDefault(poly_modulus_degree));
  parms.set_plain_modulus(PlainModulus::Batching(poly_modulus_degree, 20));
  SEALContext context(parms);
  KeyGenerator keygen(context);
  SecretKey secret_key = keygen.secret_key();
  PublicKey public_key;
  RelinKeys relin_keys;
  keygen.create_public_key(public_key);
  keygen.create_relin_keys(relin_keys);
  Encryptor encryptor(context, public_key);
  Evaluator evaluator(context);
  Decryptor decryptor(context, secret_key);
  BatchEncoder batch_encoder(context);
  size_t slots = poly_modulus_degree/2;
  size_t word_sz = 8;
  vector<Plaintext> bit_input(word_sz);
  vector<uint64_t> tmp(1);
  vector<Ciphertext> ct1_(word_sz);
  vector<Ciphertext> ct2_(word_sz);
  for (int i = 0; i < word_sz; i++) {
    tmp[0] = i % 2;
    batch_encoder.encode(tmp, bit_input[i]);
    encryptor.encrypt(bit_input[i], ct1_[i]);
    ct2_[i] = ct1_[i];
  }

  cout << "EQUALITY TEST" << endl;
  vector<Ciphertext> res_ = eq_bin(evaluator, encryptor, batch_encoder, relin_keys, ct1_, ct2_,
                                   word_sz, slots);
  
  cout << "Answer should be 1 and then all 0's: ";
  for (int i = 0; i < word_sz; i++) {
    decryptor.decrypt(res_[i], bit_input[i]);
    cout << bit_input[i];
  }
  cout << endl;

  for (int i = 0; i < word_sz; i++) {
    tmp[0] = (i+1)%2;
    batch_encoder.encode(tmp, bit_input[i]);
    encryptor.encrypt(bit_input[i], ct2_[i]);
  }

  res_ = eq_bin(evaluator, encryptor, batch_encoder, relin_keys, ct1_, ct2_,
                                   word_sz, slots);
  
  cout << "Answer should be all 0's: ";
  for (int i = 0; i < word_sz; i++) {
    decryptor.decrypt(res_[i], bit_input[i]);
    cout << bit_input[i];
  }
  cout << endl;


}
