package tests;

import static util.SourceAndSink.*;


public class StaticField {

	private static Object f;
	
	public static void foo() {
		Object a = source();
		f = a;
		bar();
	}

	private static void bar() {
		sink(f);
	}
}
