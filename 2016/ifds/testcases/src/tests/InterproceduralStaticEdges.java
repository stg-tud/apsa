package tests;

import static util.SourceAndSink.*;


public class InterproceduralStaticEdges {

	public static void foo() {
		Object a = source();
		Object d = bar(a);
		sink(d);
	}

	private static Object bar(Object b) {
		Object c = b;
		return c;
	}
}
