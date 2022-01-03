#include <iostream>
#include <helib/helib.h>
#include <string.h>
#include <chrono>
#include <vector>
#include "functional_units.hpp"

using namespace std;

/*  BGV scheme  */
int main(int argc, char *argv[]) {

  long p = 509;       // Plaintext modulus
  long r = 1;         // Hensel lifting (default = 1)
  int bits = 120;     // Number of bits of the modulus chain
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

  return EXIT_SUCCESS;
}
