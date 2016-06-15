package de.tudarmstadt.apsa.callgraphs.soot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import probe.ObjectManager;
import probe.ProbeClass;
import probe.ProbeMethod;
import soot.ClassProvider;
import soot.G;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.spark.pag.PAG;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.options.Options;

public class Main {
	public static void main(String[] args) throws IOException {
		String bin = Paths.get("bin").toAbsolutePath().toString();
		String jre = Files.list(Paths.get("jre", "1.6.0_45")).map(p -> p.toAbsolutePath().toString())
				.collect(Collectors.joining(File.pathSeparator));

		String mainClass = "de.tudarmstadt.apsa.callgraphs.examples.Shapes";

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
		CHATransformer.v().transform();

		/* Retrieve the call graph */
		CallGraph cg = Scene.v().getCallGraph();
		System.out.println(cg);

		/* Retrieve the points-to sets from the PAG */
		PAG pag = (PAG) Scene.v().getPointsToAnalysis();
		System.out.println(pag);
	}

	/**
	 * Convert a soot method to a probe method.
	 * 
	 * @param sootMethod
	 * @return
	 */
	private static ProbeMethod probeMethod(SootMethod sootMethod) {
		SootClass sootClass = sootMethod.getDeclaringClass();
		ProbeClass cls = ObjectManager.v().getClass(sootClass.toString());
		return ObjectManager.v().getMethod(cls, sootMethod.getName(), sootMethod.getBytecodeParms());
	}

	private static void addCommonDynamicClass(ClassProvider provider, String className) {
		if (provider.find(className) != null) {
			Scene.v().addBasicClass(className);
		}
	}

	private static void addCommonDynamicClasses(ClassProvider provider) {
		/*
		 * For simulating the FileSystem class, we need the implementation of
		 * the FileSystem, but the classes are not loaded automatically due to
		 * the indirection via native code.
		 */
		addCommonDynamicClass(provider, "java.io.UnixFileSystem");
		addCommonDynamicClass(provider, "java.io.WinNTFileSystem");
		addCommonDynamicClass(provider, "java.io.Win32FileSystem");

		/* java.net.URL loads handlers dynamically */
		addCommonDynamicClass(provider, "sun.net.www.protocol.file.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.ftp.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.http.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.https.Handler");
		addCommonDynamicClass(provider, "sun.net.www.protocol.jar.Handler");
	}
}