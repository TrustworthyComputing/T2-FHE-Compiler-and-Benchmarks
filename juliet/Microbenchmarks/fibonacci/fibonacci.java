class Fibonacci {

	public static void main(String[] a) {
        // Encrypted variables
        EncInt f1 = 0, f2 = 1, fi = 1, num, result;
        // Unencrypted variables
        int MAX_NUM = 50; // Maximum number of iterations

        num = PrivateTape.read(); // Read from private tape
		for (int i = 1; i <= MAX_NUM; i++) {
			result += (i == num) * fi;
			fi = f1 + f2;
			f1 = f2;
			f2 = fi;
        }
		Processor.answer(result);
	}

}
