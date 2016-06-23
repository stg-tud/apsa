package de.tudarmstadt.apsa.callgraphs.soot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import soot.G;
import soot.PhaseOptions;
import soot.PointsToAnalysis;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

public class Main {
	public static void main(String[] args) throws IOException {
		String bin = Paths.get("bin").toAbsolutePath().toString();
		String jreVersion = "1.6.0_45";// "1.4.2_11";
		String jre = Files.list(Paths.get("jre", jreVersion)).map(p -> p.toAbsolutePath().toString())
				.collect(Collectors.joining(File.pathSeparator));
		String ave = Paths.get("hello", "averroes", "averroes-lib-class.jar").toAbsolutePath().toString();
		String placeholder = Paths.get("hello", "averroes", "placeholder-lib.jar").toAbsolutePath().toString();

		String mainClass = "de.tudarmstadt.apsa.callgraphs.examples.HelloWorld";
		boolean isAverroes = true;

		/* Reset Soot */
		G.reset();

		/* Set some soot parameters */
		// Set input classes
		Options.v().classes().add(mainClass);
		if(isAverroes) Options.v().classes().add("averroes.Library");

		// Set the class path
		if(isAverroes) Options.v().set_soot_classpath(bin + File.pathSeparator + ave + File.pathSeparator + placeholder);
		else Options.v().set_soot_classpath(bin + File.pathSeparator + jre);
		

		// Set the main class
		Options.v().set_main_class(mainClass);

		// Set whole-program mode?
		Options.v().set_whole_program(true);

		/* Load the necessary classes */
		Scene.v().loadNecessaryClasses();
		Scene.v().setMainClassFromOptions(); // has to be called after loading the classes

		/* Setting entry points (i.e., main method of the main class) */
		if(isAverroes) Scene.v().setEntryPoints(entryPoints());

		/* Run the call graph transformer */
		// applyCHA();
		// applyRTA();
		// applyVTA();
		// applySpark(false);
		applySpark(isAverroes);

		/* Retrieve the call graph */
		dumpCG();

		/* Retrieve the points-to sets from the PAG */
		dumpPAG();
	}

	/**
	 * The main method of the main class set in the Soot scene is the only entry
	 * point to the call graph.
	 * 
	 * @return
	 */
	private static List<SootMethod> entryPoints() {
		List<SootMethod> result = new ArrayList<SootMethod>();
		result.add(Scene.v().getMainMethod());
		result.add(Scene.v().getMethod("<averroes.Library: void <clinit>()>"));
		return result;
	}

	/**
	 * Run the CHA analysis.
	 */
	private static void applyCHA() {
		CHATransformer.v().transform();
	}

	/**
	 * Run the RTA analysis from SPARK.
	 */
	private static void applyRTA() {
		Map<String, String> opts = new HashMap<String, String>(PhaseOptions.v().getPhaseOptions("cg.spark"));
		opts.put("enabled", "true");
		opts.put("on-fly-cg", "false");
		opts.put("rta", "true");
		SparkTransformer.v().transform("", opts);
	}

	/**
	 * Run the VTA analysis from SPARK.
	 */
	private static void applyVTA() {
		Map<String, String> opts = new HashMap<String, String>(PhaseOptions.v().getPhaseOptions("cg.spark"));
		opts.put("enabled", "true");
		opts.put("vta", "true");
		SparkTransformer.v().transform("", opts);
	}

	/**
	 * Run the default call graph analysis from SPARK.
	 * 
	 * @param isAverroes
	 */
	private static void applySpark(boolean isAverroes) {
		Map<String, String> opts = new HashMap<String, String>(PhaseOptions.v().getPhaseOptions("cg.spark"));
		opts.put("enabled", "true");
		opts.put("verbose", "true");
		// opts.put("apponly", "true"); // Similar to setting entry point to just HelloWorld.main()
		if (isAverroes) opts.put("simulate-natives", "false"); // this should only be false for SparkAve
		SparkTransformer.v().transform("", opts);
	}

	/**
	 * Dump some statistics about the pointer-assignment graph, including the
	 * points-to sets of the local variables in the main method.
	 */
	private static void dumpPAG() {
		PointsToAnalysis pts = Scene.v().getPointsToAnalysis();
		if (pts instanceof PAG) {
			PAG pag = (PAG) pts;
			System.out.println("VarNodes: " + pag.getVarNodeNumberer().size());
			System.out.println("FieldRefNodes: " + pag.getFieldRefNodeNumberer().size());
			System.out.println("AllocNodes: " + pag.getAllocNodeNumberer().size());
			System.out.println();
		}

		Scene.v().getMainMethod().getActiveBody().getLocals().stream().forEach(l -> {
			System.out.println("Points-to set of " + l + " is " + pts.reachingObjects(l));
		});
	}

	/**
	 * Dump some statistics about the call graph, including the out-edges of the
	 * main method.
	 */
	private static void dumpCG() {
		CallGraph cg = Scene.v().getCallGraph();

		/* Print out all call graph edges */
		// cg.iterator().forEachRemaining(System.out::println);

		System.out.println();
		System.out.println();
		System.out.println("The call graph has " + cg.size() + " edges.");
		System.out.println();
		System.out.println("Edges out of the main method " + Scene.v().getMainMethod());
		Iterator<Edge> edgesOutOf = cg.edgesOutOf(Scene.v().getMainMethod());
		while (edgesOutOf.hasNext()) {
			System.out.println(edgesOutOf.next());
		}

		System.out.println();
		System.out.println();
	}
}