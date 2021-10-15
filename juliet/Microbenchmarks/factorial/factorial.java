class Factorial {

	public static void main(String[] a) {
        // Encrypted variables
        EncInt num, fact, result;
        // Unencrypted variables
        int MAX_NUM = 50; // Maximum number of iterations

        num = PrivateTape.read(); // Read from private tape
        fact = 1;
		for (int i = 2; i <= MAX_NUM; i++) {
            fact *= i;
            result += (i == num) * fact;
        }
		Processor.answer(result);
	}

}
