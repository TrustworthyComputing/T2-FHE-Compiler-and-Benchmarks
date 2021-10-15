class Fibonacci {

	public static void main(String[] a) {
		// Encrypted variables
		EncInt[] keys_arr;
		EncInt[] values_arr;
		EncInt enc_input = 7, sum = 0, key, val;
        // Unencrypted variables
		int size = 6;
		keys_arr = new EncInt[size];
		values_arr = new EncInt[size];

		for (int i1 = 0 ; i1 < size ; i1++) {
			keys_arr[i1] = PrivateTape.read();
			values_arr[i1] = PrivateTape.read();
		}

		for (int i = 0 ; i < size ; i ++) {
			key = keys_arr[i];
			val = values_arr[i];
			sum += (key == enc_input) * val;
		}
		Processor.answer(sum);
	}

}

