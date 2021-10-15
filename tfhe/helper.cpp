#include "helper.hpp"

bool is_pow_of_2(int n) {
  int count = 0;
  for (int i = 0; i < 32; i++){
    count += (n >> i & 1);
  }
  return count == 1 && n > 0;
}
