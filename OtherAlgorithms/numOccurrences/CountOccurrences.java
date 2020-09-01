class CountOccurrences {

	public static void main(String[] a) {
        // Encrypted variables
        EncInt occurrences = 0, num = 2, curr = 0;
        // Unencrypted variables
        int size = 20;

        // num = PrivateTape.read();
		for (int i1 = 0 ; i1 < size ; i1++) {
			curr = PrivateTape.read();
			occurrences += (num == curr);
		}

		Processor.answer(occurrences);
	}

}
