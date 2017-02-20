package tests;
import static util.SourceAndSink.*;

public class InstanceField {


	public static void foo(boolean b) {
		Object a;
		if(b) a = source(); else a = source();
		DataStructure ds1 = new DataStructure();
		ds1.f = a;
		
		DataStructure ds2 = new DataStructure();
		ds2.f = ds1;
		
		DataStructure ds3 = (DataStructure) ds2.f;
		sink(ds3.f);
	}

	public static void bar() {
		Object a = source();
		DataStructure b = new DataStructure();
		DataStructure c = new DataStructure();
		b.f = a;
		Object d = c.f;
		sink(d);
	}
	
	private static class DataStructure {
		Object f;
	}
}
