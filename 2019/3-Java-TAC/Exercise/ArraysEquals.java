class ArraysEquals {
	
	public static void main(String [] args) {
		int[] a1 = new int[]{0};
		int[] a2 = new int[]{0};
		System.out.println(a1.equals(a2)); // <= FALSE (performs a reference comparison)
		
		int[] ac1 = new int[]{0};
		int[] ac2 = new int[]{0};
		System.out.println(java.util.Arrays.equals(ac1,ac2)); // <= TRUE (compares the content)
	}
}
