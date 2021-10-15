// exp = { 256, 2312, 4368, 6424, 29123, 46665, 22228, 57456, 61786, 50485, 56724, 16400, 9482, 28518, 59755, 19416, 4069, 31815, 57583, 15905, 1627, 17292, 62058, 46528, 34313, 40846, 55487, 2476, 59410, 10000, 11434, 36116};


class SimonDecrypt {

	public static void main(String[] a) {
		EncInt[] exp_key = new EncInt[32];
		EncInt[] pt = new EncInt[2];
		int[] ct = new int[2];
		EncInt tmp;
		// Read key
		int i = 0;
		while (i < 32) {
			exp_key[i] = PrivateTape.read();
			i++;
		}

		// Decrypt
		pt[0] = ct[0];
		pt[1] = ct[1];
		i = 0;
		while (i < 32) {
			tmp = pt[1];

			pt[1] = ((((pt[0]) ^ (((pt[1]) << 1) | ((pt[1]) >> 15))) & (((pt[1]) << 8) | ((pt[1]) >> 8))) ^ (((pt[1]) << 2) | ((pt[1]) >> 14))) ^ (exp_key[(31 - i)]);

			pt[0] = tmp;
			i++;
		}

		System.out.println(pt[0]);
		System.out.println(pt[1]);

		Processor.answer(0);
	}

}
