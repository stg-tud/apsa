# Applied Static Analysis

Technische Universität Darmstadt
Department of Computer Science
Software Technology Group

Dr. Michael Eichberg

## Exercise: Ignored Return Value

Develop an analysis that finds violations of the following rule taken from The CERT Oracle Secure Coding Standard for Java:

> EXP00-J: Do not ignore values returned by methods.

Non-compliant example:
```java
File f = new java.io.File("MyTempFile.txt");
f.delete(); // <= Return value ignored
```

Compliant example:
```java
File f = new java.io.File("MyTempFile.txt");
if(!f.delete()) {System.out.println("File could not be deleted")}; 
```

Develop the analysis by analyzing a method's bytecode. The approach should `only` handle the vast majority of standard cases; it must not handle every possible case. 

Test your analysis using the class `IgnoredReturnValue`.

Test your analysis by running it against the JDK. What do you think about the result?

You can prototype this analysis using the console or develop a small stand-alone analysis.

> If you use the console, don’t use the methods which execute the analysis in parallel (e.g., Project.parForeachMethodWithBody). The console is broken when multiple threads are used and will run into a deadlock!)