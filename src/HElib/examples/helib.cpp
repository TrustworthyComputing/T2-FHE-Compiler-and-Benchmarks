#include <iostream>
#include <helib/helib.h>
#include <string.h>
#include <chrono>

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
    const helib::PubKey& public_key = secret_key;

    // Get the EncryptedArray of the context
    const helib::EncryptedArray& ea = context.getEA();

    // Get the number of slots (phi(m))
    long nslots = ea.size();
    cout << "Number of slots: " << nslots << endl;

    // Create a vector of long with nslots elements
    helib::Ptxt<helib::BGV> ptxt(context);

    // Set it with numbers 0..nslots - 1
    for (int i = 0; i < ptxt.size(); ++i) {
        ptxt[i] = i;
    }

    cout << endl;
    cout << "Plaintext: " << ptxt.getSlotRepr() << endl;
    cout << endl;

    // Create a ciphertext
    helib::Ctxt ctxt(public_key);

    // Encrypt the plaintext using the public_key
    public_key.Encrypt(ctxt, ptxt);

    // cout << "Here's what a ciphertext looks like: " << ctxt << endl;

    // Multiply ctxt by itself
    cout << "Computing ctxt^2 homomorphically..." << endl;
    auto t1 = std::chrono::high_resolution_clock::now();
    ctxt.multiplyBy(ctxt);
    auto t2 = std::chrono::high_resolution_clock::now();
    auto duration = std::chrono::duration_cast<std::chrono::microseconds>( t2 - t1 ).count();

    // Decrypt ctxt^2
    helib::Ptxt<helib::BGV> decrypted_square(context);
    secret_key.Decrypt(decrypted_square, ctxt);
    cout << "Mult time: " << duration << endl;
    cout << "Decrypted Square: " << decrypted_square.getSlotRepr() << endl;
    cout << endl;


    // Add ctxt to itself
    cout << "Computing ctxt^2 + ctxt^2 homomorphically..." << endl;
    t1 = std::chrono::high_resolution_clock::now();
    ctxt += ctxt;
    t2 = std::chrono::high_resolution_clock::now();
    duration = std::chrono::duration_cast<std::chrono::microseconds>( t2 - t1 ).count();

    // Decrypt ctxt^2 + ctxt^2
    helib::Ptxt<helib::BGV> decrypted_sum(context);
    secret_key.Decrypt(decrypted_sum, ctxt);
    cout << "Add time: " << duration << endl;
    cout << "Decrypted Sum: " << decrypted_sum.getSlotRepr() << endl;
    cout << endl;

    // Subtract ctxt by itself
    cout << "Computing 2*ctxt^2 - 2*ctxt^2 homomorphically..." << endl;
    t1 = std::chrono::high_resolution_clock::now();
    ctxt -= ctxt;
    t2 = std::chrono::high_resolution_clock::now();
    duration = std::chrono::duration_cast<std::chrono::microseconds>( t2 - t1 ).count();

    // Decrypt 2*ctxt^2 - ctxt^2
    helib::Ptxt<helib::BGV> decrypted_sub(context);
    secret_key.Decrypt(decrypted_sub, ctxt);
    cout << "Sub time: " << duration << endl;
    cout << "Decrypted Sub: " << decrypted_sub.getSlotRepr() << endl;
    cout << endl;

    // Consume all of the noise budget

    // Create a vector of long with nslots elements
    helib::Ptxt<helib::BGV> ptxt_2(context);

    // Set it with numbers 0..nslots - 1
    for (int i = 0; i < ptxt_2.size(); ++i) {
        ptxt_2[i] = ptxt_2.size()-i;
    }

    cout << endl;
    cout << "What happens when I try to do too many operations? Like 50 multiplications." << endl;
    cout << "Plaintext 2: " << ptxt_2.getSlotRepr() << endl;
    cout << endl;

    // Create a ciphertext
    helib::Ctxt ctxt_2(public_key);

    // Encrypt the plaintext using the public_key
    public_key.Encrypt(ctxt_2, ptxt_2);

    // Do 50 consecutive multiplications to consume noise budget
    for(int i = 0; i < 50; i++) {
      ctxt_2.multiplyBy(ctxt_2);
    }

    helib::Ptxt<helib::BGV> decrypted_fail(context);
    try { secret_key.Decrypt(decrypted_fail, ctxt_2); }
    catch (...) {
      cout << "**AS EXPECTED** Decryption Fail: " << decrypted_fail.getSlotRepr() << endl;
      cout << endl;
    }
    return EXIT_SUCCESS;
}
