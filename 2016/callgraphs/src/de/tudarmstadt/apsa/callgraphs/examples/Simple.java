package de.tudarmstadt.apsa.callgraphs.examples;

public class Simple {
	public static void main(String[] args) {
		foo();
	}

	public static void foo() {
		A x = new A();
		A y = x;
		A z = new A();
		x.f = z;
		A t = bar(y);
		System.out.println(t);
	}

	public static A bar(A p) {
		return p.f;
	}
}

class A {
	A f;
}