package tests;

import static util.Logger.log;
import util.User;

public class MultipleInstanceFields {

	public static void noncompliant(User user) {
		String name = user.getName();
		DataStructure ds1 = new DataStructure();
		ds1.str = name;
		
		DataStructure ds2 = new DataStructure();
		ds2.rec = ds1;
		
		DataStructure ds3 = ds2.rec;
		log(ds3.str);
	}
	
	private static class DataStructure {
		String str;
		DataStructure rec;
	}
}
