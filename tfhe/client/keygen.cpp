#include <iostream>
#include <fstream>

#include <tfhe/tfhe.h>
#include <tfhe/tfhe_io.h>
#include <tfhe/tfhe_generic_streams.h>

int main(int argc, char** argv) {
  // Argument sanity checks.
  if (argc < 3) {
    std::cerr << "Usage: " << argv[0] << " secret_key cloud_key" << std::endl <<
      "\tsecret_key: Path to export the secret key" << std::endl <<
      "\tcloud_key: Path to export the cloud key" << std::endl;
    return EXIT_FAILURE;
  }

  // Generate a keyset.
  const int minimum_lambda = 80;
  TFheGateBootstrappingParameterSet* params =
    new_default_gate_bootstrapping_parameters(minimum_lambda);

  // Read 12 bytes from /dev/urandom.
  FILE* urandom = fopen("/dev/urandom", "r");
  uint8_t rdata[13];
  fread(&rdata, 1, 12, urandom);
  rdata[12] = '\0';
  fclose(urandom);

  // Convert bytes to integers.
  uint32_t data1 = rdata[0] | ((uint32_t)rdata[1] << 8) |
    ((uint32_t)rdata[2] << 16) | ((uint32_t)rdata[3] << 24);
  uint32_t data2 = rdata[4] | ((uint32_t)rdata[5] << 8) |
    ((uint32_t)rdata[6] << 16) | ((uint32_t)rdata[7] << 24);
  uint32_t data3 = rdata[8] | ((uint32_t)rdata[9] << 8) |
    ((uint32_t)rdata[10] << 16) | ((uint32_t)rdata[11] << 24);

  // Generate a random key.
  uint32_t seed[] = { data1, data2, data3 };
  tfhe_random_generator_setSeed(seed,3);
  TFheGateBootstrappingSecretKeySet* key = 
    new_random_gate_bootstrapping_secret_keyset(params);

  // Export the secret key to file for later use.
  std::ofstream secret_key(argv[1]);
  export_tfheGateBootstrappingSecretKeySet_toStream(secret_key, key);
  secret_key.close();

  // Export the cloud key to a file (for the cloud).
  std::ofstream cloud_key(argv[2]);
  export_tfheGateBootstrappingCloudKeySet_toStream(cloud_key, &key->cloud);
  cloud_key.close();

  // Clean up all pointers.
  delete_gate_bootstrapping_secret_keyset(key);
  delete_gate_bootstrapping_parameters(params);
  return EXIT_SUCCESS;
}
