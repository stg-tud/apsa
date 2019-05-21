theme: APSA Lecture
autoscale: true
slidenumbers: true

# Applied Static Analysis

## Inter-procedural Analysis - Basic Call Graph Algorithms

Software Technology Group  
Department of Computer Science  
Technische Universität Darmstadt  
[Dr. Michael Eichberg](mailto:m.eichberg@me.com)


---

# A Call Graph
    
 - Core data structure to conduct an inter-procedural analysis.
 
 - A call graph is a static abstraction of all method calls that a program may execute at runtime.
   - (call sites in) _methods_ are nodes
   - _calls_ are edges
  
--- 

# Example - Monomorphic Calls

^ In the following call graph, all calls can be resolved to a single call target:

```java
public class Main implements Observer {
   public static void main(String[] args) {
      Main m = new Main();
      Subject s = new Subject();
      s.addObserver(m);
      s.modify();
   }
   public void update(Observable o, Object arg) { 
      System.out.println(o+" notified me!");
   }
   static class Subject extends Observable  {
      public void modify() {
          setChanged();
          notifyObservers();
}  }  }
```  

^ ![inline](MonomorphicCallGraph.pdf)

--- 

# Example - Polymorphic Calls

^ In the following call graph, the call target is unknown. The target of the `add` call could either be a `HashSet` or an `ArrayList` object.

```java
import java.util.*;

public class Main {
   public static void main(String[] args) {
      Collection c = makeCollection(args[0]);
      c.add(args[1]);
   }
   static Collection makeCollection(String s) {
      if(s.equals("list")) {
         return new ArrayList();
      } else {
         return new HashSet();
}  }  }
```

^ ![inline](PolymorphicCallGraph.pdf)


--- 

# Call Graph Construction

> Compute a sound and precise approximation of all call edges for a given project.

^ I.e., the call graph should only contain call edges related to calls that may happen at runtime (precise) and it should contain all call edges that may happen at runtime (sound).

^ In practice, call graphs for real world programming languages are often neither sound nor precise. And even the most precise ones are still rather imprecise. If soundness is required, e.g., to prove that some software satisfies some specific properties, then it is often required that only the soundly handled subset of a specific programming language such as Java, C# or C++ is used.

Basic idea:

 - Given the set of (immediate) entry points,
 - compute all methods that can be reached from this set. 
 
^ An entry point is each method that may be called with an unknown or only partially known context. In case of a simple command line application it is the `main` method. In case of an application with a graphical user interface the set of entry points (at least) also contains the event handlers.
 
^ Depending on the kind of code-base the set of entry points may be known immediately or may be discovered along the way.

 
---

# Call Graph Construction Algorithms 

[.build-lists: true]

- Basic Algorithms:
	- Call-by Signature
	- Class Hierarchy Analysis (CHA)
 
- Advanced Algorithms (require fixed point computations):
	- Runtime Type Analysis (RTA)
	- The family: (C|M|F|X)TA (C=Class,M=Method,F=Field,X~{Method And Field})
	- Variable Type Analysis (VTA) 
	- ...

- Context-sensitive Algorithms:
	- k-CFA (CFA=Control Flow Analysis)
	- ...
	

---

# Call Graph Algorithm: Call-by Signature

Basic definition:

> For a call site `m`, assume a call edge to any method that has the same signature as `m`.
The signature is defined by the name of the method and the types of the parameters. 

^ (In Java bytecode and frameworks which analyze Java bytecode, the signature also encompasses the return type.)

---

# Call-by Signature - assessment

- Basic Properties:
   - Trivial to implement
   - Very fast
   - (In theory) Computes a sound over approximation

- Issues: 
   - very imprecise

^ Call-by signature resolution is in some cases required to correctly approximate the call graphs of libraries. The latter is in particular required if you want to perform security related analyses.

---

# Call Graph Algorithm: Class Hierarchy Analysis (CHA)

Basic definition:

> For a polymorphic (virtual) call site `m` on the declared type `T`, assume a call edge to any subclass of `T` that implements `m`.

^ Several implementation-level decisions may affect the overall precision: e.g. an abstract class `SubT` which implements `m` and is a subtype of `T` may not be considered a call target if all concrete subtypes of `SubT` also implement `m` and the set of subtypes is also known and fixed.

^ CHA based call graphs are frequently used in Java because CHA is very fast to compute and trivially to implement.
^ In Java, the call edges related to `Object`'s `toString` method along with `Iterator`'s `next` and `hasNext` methods usually make up a very significant part of the call graph (~30% in case of the JDK). E.g., the `toString` method is overwritten more than 2323 times in the Open JDK 11.0.3 and there are more than 13130 call sites!

---

# Class Hierarchy Analysis (CHA) - assessment


- Basic properties:
   - Simple to implement
   - Very fast
   - (In theory) computes a sound over approximation
   
- Issues: 
   - More precise than _Call-by Signature_, but still rather imprecise.

^ When implementing CHA or even more precise algorithms, decisions have to be made regarding the handling of incomplete class hierarchies. which are common place when analyzing real world projects.

---

# Soundiness

^ As discussed at the very beginning, most real-world static analyses are _just_ soundy; i.e., they don't all features supported by the programming language/environment. 

Sources of unsoundness[^Soundiness] w.r.t. call graph construction (for Java programs) are:

- Reflection (often very simple reflection is handled, but advanced usages are not)
- Class loading
- On-the fly class generation
- System events (`Thread.start` → `Thread.run`, finalization of objects)
- Serialization
- Native methods
- New/advanced language constructs (e.g., `invokedynamic`)

A comprehensive discussion of sources of unsoundness and the effect of implementation decisions on call graphs can be found in [^Judge].

 
---

# Implementation Differences and their Effect
Often the same conceptual algorithms are implemented very differently across frameworks (e.g., WALA, OPAL, SOOT) and therefore the call graphs vary widely in the their precision.

Example:
```
```

In the above example...



---
# Call Graph Construction for Libraries

We need call-by-signature resolution....

---
# Call Graph Construction for Libraries

We need to distinguish between the public and the private part of a library.

^ This distinction becomes easier when Java 9+ modules or OSGi Bundles are used. ...


^ <!----------------------------------------------------------------------------------------------->
^ <!---------------------------------------- REFERENCES ------------------------------------------->
^ ---

^ # References

^ [^Judge]: Judge: Identifying, Understanding, and Evaluating Sources of Unsoundness in Call Graphs; Michael Reif, Florian Kübler, Michael Eichberg, Dominik Helm, Mira Mezini, ISSTA 2019, ACM (to appear)

^ [^Soundiness]: Livshits, B., Sridharan, M., Smaragdakis, Y., Amaral, J. N., Møller, A., Lhoták, O., et al. (2015). In Defense of Soundiness: A Manifesto. Communications of the ACM, 58(2).


