class Permutations {
    public static void main(String[] a){
		int ret;
		Perms perm_obj = new Perms();
		int[] arr = new int[3];
		arr[0] = 5;
		arr[1] = 6;
		arr[2] = 7;

		ret = perm_obj.permuteArray(arr, 0, (arr.length)-1);

		Processor.answer(ret);
    }
}

class Perms {

	public int printArray(int[] arr) {
		for (int i = 0; i < (arr.length); i++) {
			System.out.println( arr[i] );
		}
		return 0;
    }

	public int permuteArray(int[] arr, int l, int r) {
		int tmp, tmp2;
	    if (l == r) {
			tmp2 = this.printArray(arr);
	    } else {
	        for (int i = l; i <= r; i++) {
				tmp = arr[l];
				arr[l] = arr[i];
				arr[i] = tmp;
	            tmp2 = this.permuteArray(arr, l+1, r);
				tmp = arr[l];
				arr[l] = arr[i];
				arr[i] = tmp;
			}
	    }
		return 0;
	}

}
