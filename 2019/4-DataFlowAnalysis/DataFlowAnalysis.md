theme: APSA Lecture
autoscale: true
slidenumbers: true

# Applied Static Analysis

## Data Flow Analysis

Software Technology Group  
Department of Computer Science  
Technische Universität Darmstadt  
[Dr. Michael Eichberg](mailto:m.eichberg@me.com)


---

# Lattice Theory

Many static analyses are based on the mathematical theory of lattices.

The lattice put the facts (often, but not always, sets) computed by an analysis in a well-defined partial order. 

Analysis are often **well-defined** functions over lattices and can then be combined and reasoned about.



---

# Example: Sign Analysis

[.build-lists: true]

 - Let's assume that we want to compute the sign of an integer value. The analysis should only return the information is definite. I.e., 

 - Instead of computing with concrete values, our analysis performs it computations using abstract values:
 	- positive (+)
	- negative (-)
	- zero
	
 - Additionally, we have to add an abstract value $$\top$$ that represents the fact that we don't know the sign of the value. 
 
 - Values that are not initialized are represented using $$\bot$$.

^ $$\top$$ is called _top_. (The least precise information. _The sound over-approximation_.)
^ $$\bot$$ is called _bottom_. (The most precise information.)


---

# Example: Sign Analysis - the lattice

^ The lattice for the previous domain is:

![inline](SignLattice.pdf)

^ The ordering reflects that $$\top$$ reflects all types of integer values.


---

# Example: Sign Analysis - example program

```scala
def select(c : Boolean): Int = {
	val a = 42
	val b = 333
	var x = 0;
	if (c)
		x = a + b;
	else
		x = a - b;
	x	
}
```

^ A possible result of the analysis could be that `a` and `b` are always positive; x is either positive or negative (or zero) ($$\top$$).

---

# Example: Constant Propagation - the lattice

^ The lattice would be:

![inline](ConstantValueLattice.pdf)

^ The ordering reflects that $$\top$$ reflects that the value is any.

^ Note that this lattice is not finite, but has finite height

^ Again $$\bot$$ denotes uninitialized (most precise) values and $$\top$$ denotes that the value is not constant.

---

# Example: Constant Propagation - example program

```scala
val z = 3
var x = 1
while(x > 0) {
  if(x == 1) {
    y = 7
  } else {
    y = z + 4
  }
  x = 3
}
```

^ A possible result of the analysis could be that `y` is always 7 (and `z` 3.)

---

# Partial Orderings

[.build-lists: true]

 - a partial ordering is a relation $$ \sqsubseteq: L \times L \rightarrow \{\mathit{true},\mathit{false}\}$$, which
	- is reflexiv: $$\forall l: l \sqsubseteq l$$
  	- is transitive: $$\forall l_1,l_2,l_3: l_1 \sqsubseteq l_2 \land l_2 \sqsubseteq l_3 \Rightarrow l_1 \sqsubseteq l_3$$
  	- is anti-symmetric: $$\forall l_1,l_2: l_1 \sqsubseteq l_2 \land l_2 \sqsubseteq l_1 \Rightarrow l_1 = l_2$$
  	
 - a partially ordered set $$(L,\sqsubseteq)$$ is a set $$L$$ equipped with a partial ordering $$\sqsubseteq$$

^ When $$x \sqsubseteq y$$ we say $$x$$ is at least as precise as $$y$$ or $$y$$ over-approximates $$x$$/$$y$$ is an over-approximation of $$y$$.


---

# Upper Bounds

[.build-lists: true]

- for $$Y \subseteq L$$ and $$l \in L$$
	- $$l$$ is an upper bound of $$Y$$, if $$\forall l' \in Y : l' \sqsubseteq l$$
  	- $$l$$ is a **least upper bound** of $$Y$$, if $$l \sqsubseteq l_0$$ whenever $$l_0$$ is also an upper bound of $$Y$$
 - if a least upper bound exists, it is unique (⊑ is anti-symmetric)
 - the least upper bound of $$Y$$ is denoted $$\bigsqcup 􏰁Y$$
we write: $$l1 ⊔ l2$$ for $$􏰁\bigsqcup\{l1, l2\}$$

^ ⊔ is also called the join operator.

	
---

# Lower Bounds

[.build-lists: true]

- for $$Y \subseteq L$$ and $$l \in L$$
  	- $$l$$ is a lower bound of $$Y$$, if $$\forall l' \in Y : l \sqsubseteq l'$$
  	- $$l$$ is a **greatest lower bound** of $$Y$$, if $$l_0 \sqsubseteq l$$ whenerver $$l_0$$ is also a lower bound of $$Y$$	

 -  if a greatest lower bound exists, it is unique (⊑ is anti-symmetric)
  - the greatest lower bound of $$Y$$ is denoted $$\mathop ⨅ 􏰁Y$$
 we write: $$l1 ⊓ l2$$ for $$\mathop ⨅\{l1, l2\}$$
	
^ ⊓ is also called the meet operator. 		

---

# Upper/Lower Bounds

A subset $$Y$$ of a partially ordered set $$L$$ need not have least upper or greatest lower bounds.

![inline](SubsetOfPartiallyOrderedSetWithNoLeastUpperBound.pdf)

^ Here, the subset is depicted in blue.

---

# (complete) Lattice
 - complete Lattice $$L = (L,\sqsubseteq,\mathop ⨅,\mathop ⨆,\top,\bot)$$
 - is a partially ordered set $$(L,\sqsubseteq)$$ such that each subset $$Y$$ has a greatest lower bound and a least upper bound.
	- $$\bot = \mathop ⨆\emptyset=\mathop ⨅ L$$
	- $$\top = \mathop ⨅\emptyset=\mathop ⨆ L$$


^ A lattice must have a unique largest element $$\top$$ and a unique smallest element $$\bot$$.

^ The least upper bound (the greatest lower bound) of a set $$Y \subseteq L$$ is always an element of $$L$$ but not necessarily an element of $$Y$$.

---

[.slidenumbers: false]

**Valid lattices**:

![inline 125%](Lattices.pdf)

**No lattice**:

![inline 125%](NoLattice.pdf)

^ Do ask yourself why the lower diagram is not a lattice?

 
---

# (complete) Lattice - example 

Example $$(\mathcal{P}(S),\subseteq)$$, $$S=\{1,2,3\}$$

 ![inline](Hasse.pdf)
 
^ The above diagram is called a _Hasse_ diagram.

^ Every finite set S defines a lattice $$(\mathcal{P}{S},\subseteq)$$ where $$\bot = \emptyset$$ and $$\top = S$$, $$x ⨆ y = x \cup y$$, and $$x ⨅ y = x \cap y$$

^ A Hasse diagram is a graphical representation of the relation of elements of a partially ordered set.

---

# Height of a lattice

The length of the longest path from $$\bot$$ to $$\top$$. 

In general, the powerset lattice has height $$|S|$$.

^ The height of the previous lattice is 3.

---

# Closure Properties

^ Construction of complete lattices:

If $$L_1,L_2,\dots,L_n$$ are lattices with finite height, then so is the (cartesian) product:

$$
L_1 \times L_2 \times \dots \times L_n = \{(x_1,x_2,\dots,x_n)| X_i \in L_i\}
$$ 

$$ 
height(L_1 \times \dots \times L_n) = height(L_1) + \dots + height(L_n)
$$

^ $$\sqsubseteq$$ is defined pointwise (i.e., $$ (l_{11},l_{21}) \sqsubseteq (l_{12},l_{22})$$ iff $$l_{11}  \sqsubseteq_1 l_{21} \land l_{11} \sqsubseteq_2 l_{21} $$) and $$ ⨆ $$ and  $$ ⨅ $$ can be computed pointwise.

---

# Two basic domains


![inline 100%](SignLattice.pdf) ![inline 100%](EvenOdd.pdf) 

---

# Creating the cross-product

Creating the cross product of the sign and even-odd lattices.

![inline 90%](EvenOddXSign.pdf)

---

# Properties of Functions

A function $$f: L_1 \rightarrow L_2$$ between partially ordered sets is **monotone** if:

$$
\forall l,l' \in L_1: l \sqsubseteq_1 l' \Rightarrow f(l) \sqsubseteq_2 f(l')  
$$

^ The composition of monotone functions is monotone. However, being monotone does not imply being extensive ($$ \forall l \in L: l \sqsubseteq f(l) $$). A function that maps all values to $$\bot$$ is clearly monotone, but not extensive.

The function $$f$$ is **distributiv** if:

$$
\forall l_1,l_2 \in L_1 : f (l_1 \sqcup l_2) = f(l_1) \sqcup f(l_2)
$$


---

# Chains

A subset $$Y \subseteq L$$ of a partially ordered set $$L = (L,\sqsubseteq)$$ is a chain if
$$
\forall l_1,l_2 \in Y: (l_1 \sqsubseteq l_2) \lor (l_2 \sqsubseteq l_1)
$$

^ That is, the values $$l_1$$ and $$l_2$$ are comparable; a chain is a possibly empty subset of $$L$$ that is totally ordered.

The chain is finite if $$Y$$ is a finite subset of $$L$$.

A sequence  $$(l_n)_n = (l_n)_{n \in N}$$ of elements in $$L$$ is an ascending chain if 
$$
n \leq m \Rightarrow l_n \sqsubseteq l_m
$$

^ A descending chain is defined accordingly.

A sequence $$(l_n)_{n}$$  eventually stabilizes iff $$\exists n_0 \in N: \forall n \in N: n \geq n_0 \Rightarrow l_n = l_{n_0}$$

---

# Interval analysis - example

^ Let's compute the range of values that an integer variable can assume at runtime.

```scala
var x = 0
while (true) {
  x = x + 1
  println(x)
}
```
^ Ask yourself how the lattice would look like?

^ 


---

# Ascending/Descending Chain Condition

 - A partially ordered set $$L$$ satisfies the Ascending Chain Condition if and only if all ascending chains eventually stabilize.

 - A partially ordered set $$L$$ satisfies the Descending Chain Condition if and only if all descending chains eventually stabilize.

^ (A lattice must not be finite to satisfy the ascending chain condition).


---

# Fixed Point

 - $$ l \in L$$ is a fixed point for $$f$$ if $$f(l) = l$$

 - A least fixed point $$l_1 \in L$$ for $$f$$ is a fixed point for $$f$$ where $$l_1 \sqsubseteq l_2$$ for every fixed point $$l_2 \in L$$ for $$f$$. 
 
---

# Equation system

 $$ x_1 = F_1(x_1,\dots,x_n) $$

 $$ \vdots $$ 

 $$ x_n = F_2(x_1,\dots,x_n) $$

where $$ x_i $$ are variables and $$ F_i: L^{n} \rightarrow L $$ is a collection of functions. If all functions are monotone then  the system has a unique least solution which is obtained as the least-fixed point of the function $$F: L^{n} \rightarrow L^{n} $$ defined by:

 $$F(x_1,\dots,x_n) = (F_1(x_1,\dots,x_n),\dots,F_n(x_1,\dots,x_n))$$

In a lattice $$L$$ with finite height, every monotone function $$f$$ has a unique least fixed point given by: $$fix(f) = \bigcup_{i \geq 0}f^{i}(\bot)$$.

---

# Data-flow analysis: Available Expressions

_Determine for each program point, which expressions must have already been computed and not later modified on all paths to the program point._

^ The following discussion of data-flow analyses uses the more common equational approach.

---

## Available Expressions - Example

[.code-highlight: 3-8 ]
```scala
         def m(initialA: Int, b: Int): Int = {
/*pc 0*/  var a = initialA 

/*pc 1*/  var x = a + b;

/*pc 2*/  val y = a * b;

/*pc 3*/  while (y > a + b) {

/*pc 4*/    a = a + 1

/*pc 5*/    x = a + b

          }
/*pc 6*/  a + x 
         }
```

^ The expression `a + b` is available every time execution reaches the test (`pc 4`).

---

## Available Expressions - gen/kill functions

 - An **expression is killed** in a block if any of the variables used in the (arithmetic) expression are modified in the block. The function $$kill: Block \rightarrow \mathcal{P}(ArithExp) $$ produces the set of killed arithmetic expressions.


 - A **generated expression** is a non-trivial (arithmetic) expression that is evaluated in the block and where none of the variables used in the expression are later modified in the block. The function $$gen: Block \rightarrow \mathcal{P}(ArithExp)$$ produces the set of generated expressions.
 
^ Typically, a block is a single statement in a program's three-address code.

^ The underlying lattice is the powerset of all expression of the program.

^ If you know that a function is pure or a field is (effectively) final it is directly possible to also take these expressions into consideration.

---

## Available Expressions - data flow equations

Let $$S$$ be our program and $$flow$$ be a flow in the program between two statements $$(pc_i,pc_j)$$.
	
$$
\begin{equation}
  AE_{entry}(pc_{i}) =
  \begin{cases}
    \emptyset & \text{if } i=0 \\
    \bigcap\{AE_{exit}(pc_h)|(pc_h,pc_i) \in \mathit{flow}(S)\} & otherwise 
  \end{cases}
\end{equation}
$$

$$
AE_{exit}(pc_{i}) =  (AE_{entry}(pc_{i}) \backslash kill(block(pc_{i})) \cup gen(block(pc_{i}))) 
$$

---

## Available Expressions - Example continued I

^ ```scala
^ /*pc 1*/  var x = a + b;
^ /*pc 2*/  val y = a * b;
^ /*pc 3*/  while (y > a + b) {
^ /*pc 4*/    a = a + 1
^ /*pc 5*/    x = a + b
^           }
^ ```

The kill/gen functions:

| pc | kill | gen |
| --- | --- | --- |
| 1 | $$ \emptyset $$   | $$\{a+b\}$$ |
| 2 | $$ \emptyset $$   | $$\{a*b\}$$ |
| 3 | $$ \emptyset $$   | $$\{a+b\}$$ |
| 4 | $$ \{a+b, a*b, a+1\} $$   | $$\emptyset$$ |
| 5 | $$ \emptyset $$    | $$\{a+b\}$$ | 

We get the following equations:

 $$AE_{entry}(pc_{1}) = \emptyset $$ <br>
 $$AE_{entry}(pc_2) = AE_{exit}(pc_1) $$ <br> 
 $$AE_{entry}(pc_3) = AE_{exit}(pc_2) \cap AE_{exit}(pc_5)$$ <br> 
 $$AE_{entry}(pc_4) = AE_{exit}(pc_3) $$ <br> 
 $$AE_{entry}(pc_5) = AE_{exit}(pc_4) $$ <br> 
 $$AE_{exit}(pc_{1}) = AE_{entry}(pc_1) \cup \{a + b\} $$ <br> 
 $$AE_{exit}(pc_{2}) = AE_{entry}(pc_{2}) \cup \{a * b\} $$ <br> 
 $$AE_{exit}(pc_{3}) = AE_{entry}(pc_{3}) \cup \{a+b\} $$ <br> 
 $$AE_{exit}(pc_{4}) = AE_{entry}(pc_{4}) \backslash \{a+b, a*b, a+1\}$$ <br> 
 $$AE_{exit}(pc_{5}) = AE_{entry}(pc_{5}) \cup \{a+b\} $$ <br> 

---

# Data-flow analysis: Naive algorithm 

> to solve the combined function: $$F(x_1,\dots,x_n) = (F_1(x_1,\dots,x_n),\dots,F_n(x_1,\dots,x_n))$$:

^ This is just a conceptual algorithm which is not to be implemented:

 $$ x = (\bot,\dots,\bot); $$ 
 $$ do \{ $$ 
 $$ t = x; $$ 
 $$ x = F(x); $$ 
 $$ \} while (x \neq t); $$ 

---

# Data-flow analysis: Chaotic Iteration

> We exploit the fact that our lattice has the structure $$L^{n}$$ to compute the solution $$(x_1,\dots,x_n)$$:

 $$ x_1=\bot;\dots,;x_n = \bot; $$ 
 $$ while (\exists i : x_i \neq F_i(x_1,\dots,x_n)) \{ $$ 
 $$ x_i = F_i(x_1,\dots,x_n); $$ 
 $$ \} $$ 


---

## Available Expressions - Example continued II

^ ```scala
^ /*pc 1*/  var x = a + b;
^ /*pc 2*/  val y = a * b;
^ /*pc 3*/  while (y > a + b) {
^ /*pc 4*/    a = a + 1
^ /*pc 5*/    x = a + b
^           }
^ ```

Solution:

| pc | kill | gen |
| --- | --- | --- |
| 1 | $$ \emptyset $$   | $$\{a+b\}$$ |
| 2 | $$\{a+b\}$$   | $$\{a+b, a*b\}$$ |
| 3 | $$\{a+b\}$$   | $$\{a+b\}$$ |
| 4 | $$\{a+b\}$$   | $$\emptyset$$ |
| 5 | $$ \emptyset $$    | $$\{a+b\}$$ | 

^ <!----------------------------------------------------------------------------------------------->
^ <!---------------------------------------- REFERENCES ------------------------------------------->
^ ---

^ # References

