theme: APSA Lecture
autoscale: true
slidenumbers: true

# Applied Static Analysis

## Monotone Frameworks

Software Technology Group  
Department of Computer Science  
Technische Universität Darmstadt  
[Dr. Michael Eichberg](mailto:m.eichberg@me.com)

For background information see:

 - Principles of Program Analysis; Flemming Nielson, Hanne Riis Nielson, and Chris Hankin; Springer, 2005


---

# Reviewing the Example Analyses

^ Compare the data flow equations for the four analyses seen so far:
  
^ Available expressions:
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

^ Reaching definitions:
$$
\begin{equation}
  RD_{entry}(pc_{i}) =
  \begin{cases}
    \emptyset & \text{if } i=0 \\
    \bigcup \{ RD_{exit}(pc_h)|(pc_h,pc_i) \in \mathit{flow}(S) \} & otherwise 
  \end{cases}
\end{equation}
$$

$$
RD_{exit}(pc_{i}) =  (RD_{entry}(pc_{i}) \backslash kill(block(pc_{i})) \cup gen(block(pc_{i}))) 
$$

^ They are very similar in their general structure.


---

# Generalized Data Flow Equations

^ These data flow equations can be generalized:
$$
\begin{equation}
  Analysis_{\circ}(pc_{i}) =
  \begin{cases}
    \iota & \text{if } i\in E \\
    \bigsqcup\{Analysis_{\bullet}(pc_h)|(pc_h,pc_i) \in F\} & otherwise 
  \end{cases}
\end{equation}
$$

$$
Analysis_{\bullet}(pc_{i}) =  f_{pc_i}(Analysis_{\circ}(pc_{i})) 
$$
^ with
$$\bigsqcup$$ being $$\bigcap$$ or $$\bigcup$$
$$F$$ either $$flow(S)$$ or $$flow^R(S)$$
$$E$$ either $$\{init(S)\}$$ or $$final(S)$$
$$\iota$$ being the initial or final analysis information
$$f_{pc_i}$$ the transfer function for $$pc_i$$

---

# Characterization of analyses

^ *Forward analyses* use $$F=flow(S)$$, $$\circ=entry$$, $$\bullet=exit$$ and $$E=\{init(S)\}$$, while
^ *Backward analyses* use $$F=flow^R(S)$$, $$\circ=exit$$, $$\bullet=entry$$ and $$E=final(S)$$

^ Analyses that require that all paths fulfill a property use $$\bigsqcup=\bigcap$$ and are called *must analyses*
^ Analyses that require at least one path to fulfill a property use $$\bigsqcup=\bigcup$$ and are called *may analyses*


---

# Monotone Framework

[.build-lists: true]

- A **monotone framework** consists of
    - a (complete) lattice $$L$$ that satisfies the *ascending chain condition* where $$\bigsqcup$$ is the least upper bound and
    - a set $$\mathcal{F}$$ of **monotone** *transfer functions*
- If the transfer functions are additionally **distributive**, we call it a **distributive framework**


---

# Instances

[.build-lists: true]

- Analyses are **instances** of a monotone framework with
    - the lattice $$L$$ and transfer functions $$\mathcal{F}$$ from the framework
    - a flow graph $$flow$$ that is usually $$flow(S)$$ or $$flow^R(S)$$
    - a set of *extremal labels* $$E$$, typically $$\{init(S)\}$$ or $$final(S)$$
    - an *extremal value* $$\iota \in L$$ for the extremal labels and
    - a mapping $f$ from statements to transfer functions in $$\mathcal{F}$$

---

# Transfer Functions from Gen/Kill Functions

The four examples additionally all had their *transfer functions* based on *gen* and *kill* functions:
$$\mathcal{F}=\{f : L \rightarrow L | f(Analysis(pc_i))=(Analysis(pc_i) \setminus kill(block(pc_i))) \cup gen(block(pc_i))\}$$

---

# Reviewing the Examples again

[.build-lists: true]

- Available expressions
    - $$L=\mathcal{P}(ArithExpr)$$ with $$\bigsqcup=\bigcap$$
    - $$flow=flow(S)$$, $$E=\{init(S)\}$$ and $$\iota=\emptyset$$

- Reaching definitions
    - $$L=\mathcal{P}(???)$$ with $$\bigsqcup=\bigcup$$
    - $$flow=flow(S)$$, $$E=\{init(S)\}$$ and $$\iota=\emptyset$$

- Very busy expressions
    - $$L=\mathcal{P}(ArithExpr)$$ with $$\bigsqcup=\bigcap$$
    - $$flow=flow^R(S)$$, $$E=final(S)$$ and $$\iota=\emptyset$$

- Live variables
    - $$L=\mathcal{P}(Var)$$ with $$\bigsqcup=\bigcup$$
    - $$flow=flow^R(S)$$, $$E=final(S)$$ and $$\iota=\emptyset$$
