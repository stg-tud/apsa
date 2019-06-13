package tests;

import static util.Logger.log;
import util.User;

public class InstanceField {

	public static void noncompliant(boolean b, User user) {
		String a;
		if(b) a = user.getName(); else a = user.getName();
		DataStructure ds1 = new DataStructure();
		ds1.f = a;
		
		DataStructure ds2 = new DataStructure();
		ds2.f = ds1;
		
		DataStructure ds3 = (DataStructure) ds2.f;
		log((String)ds3.f);
	}

	public static void compliant(User user) {
		String a = user.getName();
		DataStructure b = new DataStructure();
		DataStructure c = new DataStructure();
		b.f = a;
		String d = (String)c.f;
		log(d);
	}
	
	private static class DataStructure {
		Object f;
	}
}
