package tests;

import static util.Logger.log;
import util.User;

public class StaticField {

	private static String f;
	
	public static void foo(User user) {
		String name = user.getName();
		f = name;
		noncompliant();
	}

	private static void noncompliant() {
		log(f);
	}
}
