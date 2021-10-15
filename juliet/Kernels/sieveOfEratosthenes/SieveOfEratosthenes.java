/********************************************************
** Create a boolean array "primes[0..n]" and initialize
** all entries it as true. A value in primes[i] will
** finally be false if i is Not a primes, else true.
********************************************************/

class SieveOfEratosthenes {

    public static void main(String[] a){
        int n = 20;
        int[] primes = new int[n+1];

        for (int i = 0 ; i < (n+1) ; i++) {
            primes[i] = 1;
        }

        for (int p = 2 ; (p*p) < (n+1) ; p++) {
            for (int j = 2*p ; j < (n+1) ; j += p) { // Update all multiples of p
                primes[j] = 0;
            }
        }

        for (int prime = 2 ; prime < (n+1) ; prime++) { // Print all primes numbers
            System.out.println( prime * (primes[prime]) );
        }

		Processor.answer(0);
    }

}
