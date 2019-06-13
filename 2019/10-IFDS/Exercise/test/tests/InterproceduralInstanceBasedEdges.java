package tests;

import static util.Logger.log;
import util.User;

public class InterproceduralInstanceBasedEdges {

	public void noncompliant(User user) {
		String name = user.getName();
		String d = foo(name);
		log(d);
	}

	private String foo(String b) {
		String c = b;
		return c;
	}
}
