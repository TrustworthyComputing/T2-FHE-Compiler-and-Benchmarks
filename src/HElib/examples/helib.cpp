#include <iostream>
#include <helib/helib.h>
#include <string.h>
#include <chrono>
#include <vector>

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

    // Non-batching Encryption
    helib::Ptxt<helib::BGV> pt(context);
    pt[0] = 100;
    helib::Ctxt ct(public_key);
    public_key.Encrypt(ct, pt);

    // Copy Ctxt
    helib::Ctxt ct2(public_key);
    ct2 = ct;
    secret_key.Decrypt(pt, ct2);
    cout << "Copy : " << pt.getSlotRepr() << endl;

    // Multiply copy by 2
    ct2.multByConstant(NTL::ZZX(2));
    secret_key.Decrypt(pt, ct2);
    cout << "Copy (mult by 2) : " << pt.getSlotRepr() << endl;

    // Verify that the copy was a deep copy
    secret_key.Decrypt(pt, ct);
    cout << "Orig : " << pt.getSlotRepr() << endl;

    // Increment (NOTE: THIS AFFECTS ALL SLOTS EQUALLY)
    ct.addConstant(NTL::ZZX(1));
    secret_key.Decrypt(pt, ct);
    cout << "Increment Orig : " << pt.getSlotRepr() << endl;

    // Decrement (Same note as above)
    helib::Ptxt<helib::BGV> pt_sub(context);
    for (int i = 0; i < pt_sub.size(); ++i) {
        pt_sub[i] = 1;
    }
    ct.addConstant(pt_sub, true);
    secret_key.Decrypt(pt, ct);
    cout << "Decrement Orig :" << pt.getSlotRepr() << endl;

    ct = ct2;
    ct += ct;
    secret_key.Decrypt(pt, ct);
    cout << "Add test :" << pt.getSlotRepr() << endl;


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

    // Vectors
    helib::Ptxt<helib::BGV> tmp(context);
    vector<helib::Ptxt<helib::BGV>> ptxt_vec_;
    ptxt_vec_.push_back(ptxt);
    ptxt_vec_.push_back(ptxt);
    ptxt_vec_.resize(5, tmp);

    helib::Ctxt tmp_(public_key);
    vector<helib::Ctxt> ctxt_vec_;
    ctxt_vec_.push_back(ctxt);
    ctxt_vec_.push_back(ctxt);
    ctxt_vec_.resize(5, tmp_);

    return EXIT_SUCCESS;
}
