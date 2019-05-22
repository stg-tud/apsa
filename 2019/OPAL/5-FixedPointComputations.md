theme: APSA Lecture
autoscale: true
slidenumbers: true

# The Static Analysis Framework OPAL  

## Fixed Point Computations

**The FPCF Framework** (Subproject: `Static Analysis Infrastructure`)

Software Technology Group  
Department of Computer Science  
Technische Universität Darmstadt  
[Dr. Michael Eichberg](mailto:m.eichberg@me.com)


> If you have questions don't hesitate to join our public chat:   [Gitter](https://gitter.im/OPAL-Project/Lobby)
> If you find any issues, please directly report them: [GitHub](https://github.com/stg-tud/apsa/blob/master/2019/OPAL/5-FixedPointComputations.md)

---

# Overview

OPAL implements a general-purpose static analysis framework that facilitates a strictly modularized implementation of a wide-range of (potentially mutually dependent) static analyses.

The framework inherently supports fixed point computations and transparently handles cyclic dependencies.

--- 
# Entities and Properties

^ The development of static analyses is centered around _entities_ and _properties_:

 - _entities_ represent (virtual) elements of the source code that are of particular interest
 - _properties_ store the results of static analyses in relation to the entities. Every property belongs to exactly one property kind.

^ Entities that are generally of interest for static analyses are class and type declarations, methods, formal parameters, fields, call sites or allocation sites of new objects/arrays. Furthermore, artificial/virtual entities such as a project's call graph can also be the target of analyses. 

^ Properties are, e.g., the immutability of (all) instances of a specific class or the immutability of (all) instances of a specific type. In FPCF, it is a recurring pattern that a concrete analysis is implemented by two sub analysis: one analysis that derives a property related to a specific entity and a second analysis that basically just aggregates all results, e.g., over the type hierarchy.

---

# Property Kinds

The property kind encodes (ex- or implicitly) the lattice regarding a property's extensions and also explicitly encodes the fallback behavior if no analysis is scheduled or if an analysis is scheduled but no 

 (typically the bottom value of the lattice) that is to be used if no analysis is available that will compute a respective value for an entity.\footnote{This enables the debugging and testing of analyses in a very modular fashion} For example, in case of a thrown-exceptions analysis the lattice underlying the analysis is the subset lattice of all exception types.


---

# Examples

To get a deeper understanding how to instantiate the framework consider studying concrete implementations. In OPAL, we have implemented some analyses using the framework:

 - [TypeImmutabilityAnalysis](https://bitbucket.org/delors/opal/src/develop/OPAL/br/src/main/scala/org/opalj/br/fpcf/analyses/TypeImmutabilityAnalysis.scala)
 - [ClassImmutabilityAnalysis](https://bitbucket.org/delors/opal/src/develop/OPAL/br/src/main/scala/org/opalj/br/fpcf/analyses/ClassImmutabilityAnalysis.scala)

