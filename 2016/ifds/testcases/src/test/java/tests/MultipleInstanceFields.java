package tests;
import static util.SourceAndSink.*;

public class MultipleInstanceFields {


	public static void foo() {
		Object a = source();
		DataStructure ds1 = new DataStructure();
		ds1.f = a;
		
		DataStructure ds2 = new DataStructure();
		ds2.rec = ds1;
		
		DataStructure ds3 = ds2.rec;
		sink(ds3.f);
	}
	
	private static class DataStructure {
		Object f;
		DataStructure rec;
	}
}
