package tests;

import static util.SourceAndSink.sink;
import static util.SourceAndSink.source;

public class CorrelatedCalls {

	interface A {
		Object foo(Object x);
		Object bar(Object x);
	}
	
	class B implements A {
		@Override
		public Object foo(Object x) {
			return x;
		}

		@Override
		public Object bar(Object x) {
			return null;
		}
	}
	
	class C implements A {
		@Override
		public Object foo(Object x) {
			return null;
		}

		@Override
		public Object bar(Object x) {
			return x;
		}
	}
	
	public static void main(A a) {
		Object x = source();
		x = a.foo(x);
		x = a.bar(x);
		sink(x);
	}
}
