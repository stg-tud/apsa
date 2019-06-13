package tests;

import static util.Logger.log;
import util.User;

public class CorrelatedCalls {

	interface A {
		String foo(String x);
		String bar(String x);
	}
	
	class B implements A {
		@Override
		public String foo(String x) {
			return x;
		}

		@Override
		public String bar(String x) {
			return null;
		}
	}
	
	class C implements A {
		@Override
		public String foo(String x) {
			return null;
		}

		@Override
		public String bar(String x) {
			return x;
		}
	}
	
	public static void compliant(A a, User user) {
		String x = user.getName();
		x = a.foo(x);
		x = a.bar(x);
		log(x);
	}
}
