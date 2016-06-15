package de.tudarmstadt.apsa.callgraphs.examples;

public class Shapes {
	public static void main(String[] args) {
		Shape s;
		if (args.length > 2)
			s = new Circle();
		else
			s = new Square();

		s.draw();
	}
}

abstract class Shape {
	abstract void draw();
}

class Circle extends Shape {
	void draw() {
		System.out.println("Drawing a circle.");
	}
}

class Square extends Shape {
	void draw() {
		System.out.println("Drawing a square.");
	}
}
