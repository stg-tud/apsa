package tests;

import static util.SourceAndSink.*;


public class Arrays {

	public static void foo() {
		Object a = source();
		Object[] arr = new Object[2];
		arr[0] = a;
		Object b = arr[1];
		sink(b);
	}
	
	public static void bar() {
		Object a = source();
		Object[] arr = new Object[1];
		arr[0] = a;
		Object b = arr[0];
		sink(b);
	}
}
