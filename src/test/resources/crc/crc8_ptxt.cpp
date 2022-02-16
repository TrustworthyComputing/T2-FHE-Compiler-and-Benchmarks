#include <iostream>
#include <cstdint>

using namespace std;

// uint8_t crc8_table(uint8_t data) {
// 		uint8_t crc = data;
//     int j;
// 		for (j=0; j<8; j++) {
// 			crc = (crc << 1) ^ ((crc & 0x80) ? 0x07 : 0);
// 		}
// 		return crc;
// }

uint8_t crc8_table(uint8_t data) {
		uint8_t crc = data;
    uint8_t tmp;
    int j;
		for (j=0; j<8; j++) {
      tmp = crc >> 7;
      tmp = tmp ? 0x07 : 0;
      crc = tmp ^ (crc << 1);
		}
		return crc;
}

int main() {
  uint8_t data = 0x42;
  uint8_t hash = crc8_table(data);
  cout << "Final Answer: " << int(hash) << endl;
  return 0;
}