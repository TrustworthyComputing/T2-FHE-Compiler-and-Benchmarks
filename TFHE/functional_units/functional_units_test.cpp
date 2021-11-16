#include <iostream>
#include <tfhe/tfhe.h>
#include <tfhe/tfhe_io.h>
#include "functional_units.hpp"

using namespace std;

int main() {

  // Generate keypair
  const int minimum_lambda = 110;
  TFheGateBootstrappingParameterSet* params = new_default_gate_bootstrapping_parameters(minimum_lambda);

  //generate key with seed
  uint32_t seed[] = { 314, 1592, 657 };
  tfhe_random_generator_setSeed(seed,3);
  TFheGateBootstrappingSecretKeySet* key = new_random_gate_bootstrapping_secret_keyset(params);
  
  // BINARY TESTS

  // Enc/Dec
  LweSample* ctxt_ = e_client(32,16,key);
  assert(d_client(16, ctxt_, key) == 32);
  
  // Rotation 
  rotate_inplace(ctxt_, LEFT, 2, 16, &key->cloud);
  assert(d_client(16, ctxt_, key) == (32 << 2));
  rotate_inplace(ctxt_, RIGHT, 2, 16, &key->cloud);
  assert(d_client(16, ctxt_, key) == 32);

  // Add 
  LweSample* const_ = e_cloud(16, 16, &key->cloud);
  add(ctxt_, ctxt_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt_, key) == (32+16));

  // Subtract (result must be different than operands)
  LweSample* ctxt2_ = e_cloud(0, 16, &key->cloud);
  sub(ctxt2_, ctxt_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key) == 32);

  // Multiply
  mult(ctxt_, ctxt2_, ctxt_, 16, &key->cloud);
  assert(d_client(16, ctxt_, key) == (32*48));

  // Increment
  inc(ctxt_, ctxt_, 16, &key->cloud);
  assert(d_client(16, ctxt_, key) == (32*48+1));

  // Equality 
  LweSample* bool_ = e_cloud(0, 1, &key->cloud);
  eq(bool_, ctxt_, ctxt_, 16, &key->cloud);
  assert(d_client(1, bool_, key) == 1);
  eq(bool_, ctxt_, ctxt2_, 16, &key->cloud);
  assert(d_client(1, bool_, key) == 0);

  // Less-than
  leq(bool_, ctxt2_, ctxt_, 16, &key->cloud);
  assert(d_client(1, bool_, key) == 1);
  leq(bool_, ctxt_, ctxt2_, 16, &key->cloud);
  assert(d_client(1, bool_, key) == 0);

  return 0;
}