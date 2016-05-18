package tests;

import static util.SourceAndSink.*;


public class Category2Values {

	public static void foo() {
		Object a = source();
		double x = 2.0;
		bar(x, a);
	}

	private static void bar(double x, Object a) {
		Object b = a;
		double y = x;
		sink(b);
	}
}
