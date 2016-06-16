package de.tudarmstadt.apsa.callgraphs.soot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import soot.G;
import soot.PhaseOptions;
import soot.PointsToAnalysis;
import soot.Scene;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.spark.pag.PAG;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.options.Options;

public class Main {
	public static void main(String[] args) throws IOException {
		String bin = Paths.get("bin").toAbsolutePath().toString();
		String jre = Files.list(Paths.get("jre", "1.6.0_45")).map(p -> p.toAbsolutePath().toString())
				.collect(Collectors.joining(File.pathSeparator));

		String mainClass = "de.tudarmstadt.apsa.callgraphs.examples.Coll";

		/* Reset Soot */
		G.reset();

		/* Set some soot parameters */
		// Set input classes
		// Options.v().set_process_dir(Arrays.asList("bin"));
		Options.v().classes().add(mainClass);

		// Set the class path
		// Options.v().set_prepend_classpath(true);
		Options.v().set_soot_classpath(bin + File.pathSeparator + jre);

		// Set the main class
		Options.v().set_main_class(mainClass);

		// Set whole-program mode?
		Options.v().set_whole_program(true);

		/* Load the necessary classes */
		Scene.v().loadNecessaryClasses();
		Scene.v().setMainClassFromOptions(); // has to be called after loading
												// the classes

		/* Setting entry points (i.e., main method of the main class) */
		Scene.v().setEntryPoints(Arrays.asList(Scene.v().getMainMethod()));

		/* Run the call graph transfomer */
//		applyCHA();
		applyRTA();
//		applyVTA();

		/* Retrieve the call graph */
		dumpCG();

		/* Retrieve the points-to sets from the PAG */
		dumpPAG();
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