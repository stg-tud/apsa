package tests;
import static util.SourceAndSink.sink;
import static util.SourceAndSink.*;


public class Sanitization {

	public static void foo() {
		Object a = source();
		sanitize(a);
		sink(a);
	}
}

