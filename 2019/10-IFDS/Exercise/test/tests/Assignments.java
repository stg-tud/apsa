package tests;

import static util.Logger.log;
import util.User;

public class Assignments {

	public static void noncompliant(User user) {
		String name = user.getName();
		String theNameAgain = name;
		log(theNameAgain);
	}
}
