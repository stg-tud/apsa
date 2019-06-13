package tests;

import static util.Logger.log;
import util.User;

public class Arrays {

	public static void compliant(User user) {
		String name = user.getName();
		String[] arr = new String[2];
		arr[0] = name;
		String notTheName = arr[1];
		log(notTheName);
	}
	
	public static void noncompliant(User user) {
		String name = user.getName();
		String[] arr = new String[1];
		arr[0] = name;
		String theName = arr[0];
		log(theName);
	}
}
