#include <iostream>
#include <helib/helib.h>
#include <string.h>
#include <chrono>
#include <vector>
#include "functional_units.hpp"

using namespace std;

/*  BGV scheme  */
int main(int argc, char *argv[]) {

  long p = 19;       // Plaintext modulus
  long r = 1;         // Hensel lifting (default = 1)
  int bits = 200;     // Number of bits of the modulus chain
  long c = 2;         // Number of columns of Key-Switching matrix (default = 2 or 3)
  long m = 14481;     // Cyclotomic polynomial - defines phi(m)

  cout << "Initializing context object..." << endl;

  // Initialize context
  // This object will hold information about the algebra created from the
  // previously set parameters
  helib::Context context = helib::ContextBuilder<helib::BGV>()
                                .m(m)
                                .p(p)
                                .r(r)
                                .bits(bits)
                                .c(c)
                                .build();

  // Print the security level
  std::cout << "Security: " << context.securityLevel() << std::endl;

  // Secret key management
  cout << "Creating secret key..." << endl;

  // Create a secret key associated with the context
  helib::SecKey secret_key = helib::SecKey(context);

  // Generate the secret key
  secret_key.GenSecKey();

  cout << "Generating key-switching matrices..." << endl;

  // Compute key-switching matrices that we need (for multiplication and bootstrapping)
  helib::addSome1DMatrices(secret_key);

  // Public key management
  // Set the secret key (upcast: SecKey is a subclass of PubKey)
  helib::PubKey& public_key = secret_key;

  // Get the EncryptedArray of the context
  const helib::EncryptedArray& ea = context.getEA();

  // Get the number of slots (phi(m))
  long nslots = ea.size();
  cout << "Number of slots: " << nslots << endl;

  // Test binary array Enc/Dec 
  vector<helib::Ctxt> bin_test = encrypt_num_to_binary_array(public_key, 
    context, 19, 8);
  assert(decrypt_binary_array(secret_key, context, bin_test) == 19);

  // Test binary array Enc/Dec (Batched)
  helib::Ctxt bin_batch = encrypt_num_to_binary_array_batch(public_key, context,
    87, nslots, 1);
  assert(decrypt_binary_array_batch(secret_key, context, bin_batch, 8, 1) == 87);

  // Test integer Enc/Dec (Batched)
  vector<uint64_t> ptxt_batch = {1, 2, 3, 4, 5};
  helib::Ctxt batch = encrypt_nums_to_array_batch(public_key, context, ptxt_batch,
    5, nslots, 1);
  vector<uint64_t> recovered_ptxt = decrypt_array_batch_to_nums(secret_key, 
    context, batch, nslots, 1);
  for (int i = 0; i < ptxt_batch.size(); i++) {
    assert(ptxt_batch[i] == recovered_ptxt[i]);
  }

  // Test XOR (Batched)
  helib::Ctxt xor_result = exor(public_key, bin_batch, bin_batch);
  assert(decrypt_binary_array_batch(secret_key, context, xor_result, 8, 1) == 0);

  // Test Equality (Batched)
  helib::Ctxt eq_result = eq_bin_batched(public_key, context, bin_batch, bin_batch,
    nslots, ea, 1);
  assert(decrypt_binary_array_batch(secret_key, context, eq_result, 8, 1) == 0xFF);
  
  // Test Less-Than (Batched)
  helib::Ctxt lt_result = lt_bin_batched(public_key, bin_batch, bin_batch, nslots);
  assert(decrypt_binary_array_batch(secret_key, context, lt_result, 8, 1) == 0);

  // Test Less-Than (Batched Plain)
  helib::Ptxt<helib::BGV> pt_test(context);
  for (int i = 0; i < pt_test.size(); i++) {
    pt_test[i] = 0;
  }
  lt_result = lt_bin_batched_plain(public_key, bin_batch, pt_test, nslots);
  assert(decrypt_binary_array_batch(secret_key, context, lt_result, 8, 1) == 0);

  // Test Less-Than-Or-Equals (Batched Plain) 
  // NOTE: THROWS A DIVISION ERROR IF PTXT ARRAY IS ALL ZERO
  for (int i = 0; i < pt_test.size(); i++) {
    pt_test[i] = 1;
  }
  lt_result = lte_bin_batched_plain(public_key, bin_batch, pt_test, nslots);
  assert(decrypt_binary_array_batch(secret_key, context, lt_result, 8, 1) == 255);
  
  // Test Equality (Integer)
  helib::Ctxt eq_int_res = eq(public_key, batch, batch, p, nslots);
  assert(decrypt_binary_array_batch(secret_key, context, eq_int_res, 5, 1) == 31);
  vector<uint64_t> ptxt_batch_2 = {6, 7, 8, 9, 10};
  helib::Ctxt batch_2 = encrypt_nums_to_array_batch(public_key, context, ptxt_batch_2,
    5, nslots, 1);
  eq_int_res = eq(public_key, batch, batch_2, p, nslots);
  assert(decrypt_binary_array_batch(secret_key, context, eq_int_res, 5, 1) == 0);
  
  // Test Less-Than (Integer)
  helib::Ctxt lt_int_res = lt(public_key, batch_2, batch, p, nslots);
  assert(decrypt_binary_array_batch(secret_key, context, lt_int_res, 5, 1) == 0);
  lt_int_res = lt(public_key, batch, batch_2, p, nslots);
  assert(decrypt_binary_array_batch(secret_key, context, lt_int_res, 5, 1) == 31);
  
  helib::Ptxt<helib::BGV> ptxt_test(context);
  for (int i = 0; i < nslots; i++) {
    pt_test[i] = 1;
  }

  eq_int_res = eq_plain(public_key, batch, ptxt_test, p, nslots);
  cout << decrypt_binary_array_batch(secret_key, context, eq_int_res, 5, 1) << endl;
  
  return EXIT_SUCCESS;
}
