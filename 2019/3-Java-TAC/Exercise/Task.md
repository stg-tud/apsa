# Applied Static Analysis

Software Technology Group  
Department of Computer Science  
Technische Universität Darmstadt  
[Dr. Michael Eichberg](mailto:eichberg@informatik.tu-darmstadt.de)

# Simple Data-flow Analysis

You should use `MyOPALProject` as a template. That project is preconfigured to use the latest snapshot version of OPAL. You can clone the project using:  
`git clone --depth 1 git@bitbucket.org:OPAL-Project/myopalproject.git Project`

For further details regarding the development of static analysis using OPAL see the OPAL tutorial.

## Use Arrays.equals

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

***Tasks***

 1. Test your analysis using the class `ArraysEquals`.

