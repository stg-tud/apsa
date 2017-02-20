package tests;

import static util.SourceAndSink.*;


public class InterproceduralInstanceBasedEdges {

	public void foo() {
		Object a = source();
		Object d = bar(a);
		sink(d);
	}

	private Object bar(Object b) {
		Object c = b;
		return c;
	}
}
