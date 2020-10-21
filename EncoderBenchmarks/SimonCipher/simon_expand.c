#include <stdio.h>
#include <stdlib.h>
#include <inttypes.h>
#include <stdint.h>

#define ROUNDS 32
#define KEY_WORDS 4
#define WORD_SIZE 16

#define ROR(x, r) ((x >> r) | (x << ((sizeof(uint16_t) * 8) - r)))
#define ROL(x, r) ((x << r) | (x >> ((sizeof(uint16_t) * 8) - r)))

#define WORD_MASK ((0x1ull << (WORD_SIZE&63)) - 1)
#define C ((0xffffffffffffffffull ^ 0x3ull) & WORD_MASK)

uint16_t z[5][62] = {
    {1,1,1,1,1,0,1,0,0,0,1,0,0,1,0,1,0,1,1,0,0,0,0,1,1,1,0,0,1,1,0,1,1,1,1,1,0,1,0,0,0,1,0,0,1,0,1,0,1,1,0,0,0,0,1,1,1,0,0,1,1,0},
    {1,0,0,0,1,1,1,0,1,1,1,1,1,0,0,1,0,0,1,1,0,0,0,0,1,0,1,1,0,1,0,1,0,0,0,1,1,1,0,1,1,1,1,1,0,0,1,0,0,1,1,0,0,0,0,1,0,1,1,0,1,0},
    {1,0,1,0,1,1,1,1,0,1,1,1,0,0,0,0,0,0,1,1,0,1,0,0,1,0,0,1,1,0,0,0,1,0,1,0,0,0,0,1,0,0,0,1,1,1,1,1,1,0,0,1,0,1,1,0,1,1,0,0,1,1},
    {1,1,0,1,1,0,1,1,1,0,1,0,1,1,0,0,0,1,1,0,0,1,0,1,1,1,1,0,0,0,0,0,0,1,0,0,1,0,0,0,1,0,1,0,0,1,1,1,0,0,1,1,0,1,0,0,0,0,1,1,1,1},
    {1,1,0,1,0,0,0,1,1,1,1,0,0,1,1,0,1,0,1,1,0,1,1,0,0,0,1,0,0,0,0,0,0,1,0,1,1,1,0,0,0,0,1,1,0,0,1,0,1,0,0,1,0,0,1,1,1,0,1,1,1,1}
};

void simon_expand(uint16_t K[static ROUNDS]) {
    uint16_t tmp;
    for (int i = KEY_WORDS; i < ROUNDS; ++i) {
        tmp = ROR(K[i-1], 3);
        tmp ^= K[i-3];
        tmp ^= ROR(tmp, 1);
        K[i] = K[i-KEY_WORDS] ^ z[0][(i-KEY_WORDS) % 62] ^ tmp ^ C;
    }
}
int main(void) {
    uint16_t key[ROUNDS] = { 0  };
    key[3] = 0x1918; key[2] = 0x1110;
    key[1] = 0x0908; key[0] = 0x0100;

    simon_expand(key);

    for (int i = 0; i < ROUNDS; ++i) {
        printf("%" PRIu16 " ", key[i]);
    }
    printf("\n");
    return EXIT_SUCCESS;
}
