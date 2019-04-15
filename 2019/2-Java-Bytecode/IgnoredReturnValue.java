import java.io.File;

class IgnoredReturnValue {
	
	public static void main(String [] args) {
		File fa = new File("MyTempFileA.txt");
		fa.delete(); // <= Return value ignored
		
		File fb = new File("MyTempFileB.txt");
		if(!fb.delete()) { System.out.println(fb + " could not be deleted"); }; 
	}
}
