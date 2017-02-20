package util;

public class SourceAndSink {

	public static Object source() {
		return new Object();
	}
	
	public static void sink(Object obj) {
		System.out.println(obj);
	}

	public static void sanitize(Object obj) {
		
	}
}
