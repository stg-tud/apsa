# Applied Static Analysis

Technische Universität Darmstadt
Department of Computer Science
Software Technology Group

Dr. Michael Eichberg

## Exercise: Arrays.equals

Develop an analysis which finds violations of the following rule taken from The CERT Oracle Secure Coding Standard for Java:

> EXP02-J: Use the two-argument Arrays.equals() method to compare the contents of arrays.

Non-compliant example:
```java
int[] a1 = new int[]{0};
int[] a2 = new int[]{0};
a1.equals(a2); // <= FALSE (performs a reference comparison)
```

Compliant example:
```java
int[] a1 = new int[]{0};
int[] a2 = new int[]{0};
Arrays.equals(a1,a2); // <= TRUE (compares the content)
```

Recall that arrays are objects and that it is therefore possible to call those methods (e.g., wait, notify and equals) on arrays which are defined by `java.lang.Object`. Furthermore, the declared receiver of the call will be the class type `java.lang.Object`.

You can prototype this analysis using the console or develop a small stand-alone analysis. 

> **If you use the console, don't use the methods which execute the analysis in parallel (e.g., Project.parForeachMethodWithBody). The console is broken when multiple threads are used and will run into a deadlock!)**

Test your analysis using the class `ArraysEquals`.

