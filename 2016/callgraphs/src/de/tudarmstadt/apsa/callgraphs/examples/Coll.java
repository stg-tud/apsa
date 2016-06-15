package de.tudarmstadt.apsa.callgraphs.examples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class Coll {
	public static void main(String[] args) {
		Collection c = makeCollection(args[0]);
		c.add("elem");
	}

	static Collection makeCollection(String s) {
		if (s.equals("list")) {
			return new ArrayList();
		} else {
			return new HashSet();
		}
	}
}