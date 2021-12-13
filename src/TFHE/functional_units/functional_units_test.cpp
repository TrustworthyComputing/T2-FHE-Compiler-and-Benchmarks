#include <iostream>
#include <tfhe/tfhe.h>
#include <tfhe/tfhe_io.h>
#include "functional_units.hpp"

using namespace std;

int main() {

  // Generate keypair
  const int minimum_lambda = 80;
  TFheGateBootstrappingParameterSet* params =
    new_default_gate_bootstrapping_parameters(minimum_lambda);

  //generate key with seed
  uint32_t seed[] = { 314, 1592, 657 };
  tfhe_random_generator_setSeed(seed,3);
  TFheGateBootstrappingSecretKeySet* key =
    new_random_gate_bootstrapping_secret_keyset(params);

  vector<LweSample*> ctxt_;
  vector<LweSample*> ctxt2_;
  vector<LweSample*> const_;
  vector<LweSample*> bool_;
  vector<uint16_t> ans = {1, 2};
  vector<uint32_t> ans_big = {1, 2};


  // BINARY TESTS

  // Enc/Dec
  vector<uint32_t> ptxt = {53,1};
  vector<uint32_t> rec_ptxt;
  ctxt_ = e_client(ptxt, 16, key);
  rec_ptxt = d_client(16, ctxt_, key);
  assert(d_client(16, ctxt_, key)[0] == 53);
  assert(d_client(16, ctxt_, key)[1] == 1);

  // Copy
  copy(ctxt2_, ctxt_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == 53);
  assert(d_client(16, ctxt2_, key)[1] == 1);

  // Rotation
  rotate_inplace(ctxt_, LEFT, 2, 16, &key->cloud);
  assert(d_client(16, ctxt_, key)[0] == (53 << 2));
  assert(d_client(16, ctxt_, key)[1] == (1 << 2));
  rotate_inplace(ctxt_, RIGHT, 2, 16, &key->cloud);
  assert(d_client(16, ctxt_, key)[0] == 53);
  assert(d_client(16, ctxt_, key)[1] == 1);

  cout << "Finished rotations!" << endl;

  // Add
  vector<uint32_t> const_val = {8,8};
  const_ = e_cloud(const_val, 16, &key->cloud);
  add(ctxt_, ctxt_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt_, key)[0] == (53+8));
  assert(d_client(16, ctxt_, key)[1] == (1+8));

  // Subtract (result must be different than operands)
  const_val = {0,0};
  ctxt2_ = e_cloud(const_val, 16, &key->cloud);
  sub(ctxt2_, ctxt_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == 53);
  assert(d_client(16, ctxt2_, key)[1] == 1);

  // Multiply
  mult(ctxt_, ctxt2_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt_, key)[0] == (53*8));
  assert(d_client(16, ctxt_, key)[1] == (1*8));

  // Increment
  inc(ctxt_, ctxt_, 16, &key->cloud);
  assert(d_client(16, ctxt_, key)[0] == (53*8+1));
  assert(d_client(16, ctxt_, key)[1] == (1*8+1));

  // Equality
  bool_ = e_cloud(const_val, 1, &key->cloud);
  eq(bool_, ctxt_, ctxt_, 16, &key->cloud);
  assert(d_client(1, bool_, key)[0] == 1);
  assert(d_client(1, bool_, key)[1] == 1);
  eq(bool_, ctxt_, ctxt2_, 16, &key->cloud);
  assert(d_client(1, bool_, key)[0] == 0);
  assert(d_client(1, bool_, key)[1] == 0);

  // Less-than
  lt(bool_, ctxt2_, ctxt_, 16, &key->cloud);
  assert(d_client(1, bool_, key)[0] == 1);
  assert(d_client(1, bool_, key)[1] == 1);
  lt(bool_, ctxt_, ctxt2_, 16, &key->cloud);
  assert(d_client(1, bool_, key)[0] == 0);
  assert(d_client(1, bool_, key)[0] == 0);

  // NOT
  ctxt_ = e_client(ans_big, 16, key);
  ctxt2_ = e_client(ans_big, 16, key);
  ans[0] = ~ans[0];
  ans[1] = ~ans[1];
  e_not(ctxt2_, ctxt_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);

  // AND
  vector<uint32_t> sixteen = {16, 16};
  const_ = e_cloud(sixteen, 16, &key->cloud);
  ans[0] = 1 & 16;
  ans[1] = 2 & 16;
  e_and(ctxt2_, ctxt_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);
  ans[0] = 16 & 16;
  ans[1] = 16 & 16;
  e_and(ctxt2_, const_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);

  // OR
  ans[0] = 1 | 16;
  ans[1] = 2 | 16;
  e_or(ctxt2_, ctxt_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);
  ans[0] = 16 | 16;
  ans[1] = 16 | 16;
  e_or(ctxt2_, const_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);


  // NAND
  ans[0] = ~(1 & 16);
  ans[1] = ~(2 & 16);
  e_nand(ctxt2_, ctxt_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);
  ans[0] = ~(16 & 16);
  ans[1] = ~(16 & 16);
  e_nand(ctxt2_, const_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);


  // NOR
  ans[0] = ~(1 | 16);
  ans[1] = ~(2 | 16);
  e_nor(ctxt2_, ctxt_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);
  ans[0] = ~(16 | 16);
  ans[1] = ~(16 | 16);
  e_nor(ctxt2_, const_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);

  // XOR
  ans[0] = 1 ^ 16;
  ans[1] = 2 ^ 16;
  e_xor(ctxt2_, ctxt_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);
  ans[0] = 16 ^ 16;
  ans[1] = 16 ^ 16;
  e_xor(ctxt2_, const_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);

  // XNOR
  ans[0] = ~(1 ^ 16);
  ans[1] = ~(2 ^ 16);
  e_xnor(ctxt2_, ctxt_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);
  ans[0] = ~(16 ^ 16);
  ans[1] = ~(16 ^ 16);
  e_xnor(ctxt2_, const_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);

  // MUX
  ans[0] = 1 * (!0) + 16 * 0;
  ans[1] = 2 * (!0) + 16 * 0;
  sixteen = {0xffff, 0xffff};
  bool_ = e_cloud(sixteen, 16, &key->cloud);
  e_mux(ctxt2_, bool_, ctxt_, const_, 16, &key->cloud);
  cout << d_client(16, ctxt2_, key)[0] << " " << d_client(16, ctxt2_, key)[1] << endl;
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);
  ans[0] = 16 * (!0) + 1 * 0;
  ans[1] = 16 * (!0) + 2 * 0;
  sixteen = {0x0000, 0x0000};
  bool_ = e_cloud(sixteen, 16, &key->cloud);
  e_mux(ctxt2_, bool_, ctxt_, const_, 16, &key->cloud);
  assert(d_client(16, ctxt2_, key)[0] == ans[0]);
  assert(d_client(16, ctxt2_, key)[1] == ans[1]);

  // // INTEGER TESTS
  //
  // const int ptxt_mod = 100;
  //
  // // Client Enc/Dec
  // ctxt_ = e_client_int(4, ptxt_mod, key);
  // assert(d_client_int(ptxt_mod, ctxt_, key) == 4);
  // ctxt_ = e_client_int(-10, ptxt_mod, key);
  // assert(d_client_int(ptxt_mod, ctxt_, key) == (ptxt_mod-10));
  //
  // // Bit to Int (1 maps to 1, 0 maps to -1)
  // ctxt_ = e_client(1, 1, key);
  // bool_ = e_client(0, 1, key);
  // ctxt2_ = e_bin_to_int(ctxt_, ptxt_mod, &key->cloud);
  // bool_ = e_bin_to_int(bool_, ptxt_mod, &key->cloud);
  // add_int(ctxt2_, ctxt2_, ctxt2_, &key->cloud);
  // assert(d_client_int(ptxt_mod, ctxt2_, key) == 2);
  // add_int(ctxt2_, ctxt2_, bool_, &key->cloud);
  // assert(d_client_int(ptxt_mod, ctxt2_, key) == 1);
  //
  // // Int to Bit (top 1/2 of range maps to 1, bottom 1/2 maps to 0)
  // ctxt_ = e_client_int(49, ptxt_mod, key);
  // ctxt_ = e_int_to_bin(ctxt_, &key->cloud);
  // assert(d_client(1, ctxt_, key) == 0);
  // ctxt_ = e_client_int(51, ptxt_mod, key);
  // ctxt_ = e_int_to_bin(ctxt_, &key->cloud);
  // assert(d_client(1, ctxt_, key) == 1);
  //
  // // Add
  // ctxt_ = e_client_int(21, ptxt_mod, key);
  // ctxt2_ = e_cloud_int(13, ptxt_mod, &key->cloud);
  // add_int(ctxt_, ctxt_, ctxt2_, &key->cloud);
  // assert(d_client_int(ptxt_mod, ctxt_, key) == (21+13));
  //
  // // Sub
  // sub_int(ctxt_, ctxt_, ctxt2_, &key->cloud);
  // assert(d_client_int(ptxt_mod, ctxt_, key) == 21);
  //
  // // Mult w/ Constant
  // ctxt_ = e_client_int(10, ptxt_mod, key);
  // mult_plain_int(ctxt2_, ctxt_, 4, &key->cloud);
  // assert(d_client_int(ptxt_mod, ctxt2_, key) == (10*4));
  //
  // // Cloud Enc/Dec
  // ctxt_ = e_cloud_int(4, ptxt_mod, &key->cloud);
  // assert(d_client_int(ptxt_mod, ctxt_, key) == 4);
  // ctxt_ = e_cloud_int(-10, ptxt_mod, &key->cloud);
  // assert(d_client_int(ptxt_mod, ctxt_, key) == (ptxt_mod-10));

  return 0;
}
