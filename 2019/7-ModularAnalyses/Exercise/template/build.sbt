name                          := "Allocation Freeness"

organization    in ThisBuild 	:= "org.example"
version         in ThisBuild 	:= "0.1.0-SNAPSHOT"
scalaVersion    in ThisBuild 	:= "2.12.8"

// fork         in run          := true
// javaOptions 	in run          += "-Xmx4G"        // BETTER: javaOptions in run += "-Xmx16G"
// javaOptions 	in run          += "-Xms2G"
// javaOptions 	in ThisBuild    ++= Seq(
// 	 "-Dorg.opalj.threads.CPUBoundTasks=2",        // Number of physical (not hyperthreaded) cores/CPUs
// 	 "-Dorg.opalj.threads.IOBoundTasks=3"          // Number of (hyperthreaded) cores * [1,5...3]
// )

resolvers in ThisBuild ++= Seq(Opts.resolver.sonatypeSnapshots)

libraryDependencies += "de.opal-project" %% "framework" % "3.0.0-SNAPSHOT" withJavadoc() withSources()
