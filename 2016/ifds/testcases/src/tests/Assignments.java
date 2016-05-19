package tests;

import static util.SourceAndSink.*;

public class Assignments {

	public static void foo() {
		Object a = source();
		Object b = a;
		sink(b);
	}
}
