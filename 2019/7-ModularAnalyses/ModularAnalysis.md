theme: APSA Lecture
autoscale: true
slidenumbers: true

# Applied Static Analysis

## Modular Analyses

Software Technology Group  
Department of Computer Science  
Technische Universität Darmstadt  
[Dr. Michael Eichberg](mailto:m.eichberg@me.com)

> If you find any issues, please directly report them: [GitHub](https://github.com/stg-tud/apsa/blob/master/2019/7-ModularAnalyses/ModularAnalyses.md)

---

# Problem statement

> The result of analyses can be improved by complementary information (e.g. information derived by other analysis)

- Integrating different analyses is challenging
   - it should be possible to reason about each analysis' correctness individually 
   - it should be possible to determine the impact of individual analyses on the overall performance and precision 
   - inter-analysis cyclic dependencies need to be identified and handled
   - running all analyses always may


---

# Example of inter-analysis dependencies

![inline](AnalysisDependencies.pdf)

^ The image is taken from [^FPCF].

---

# Techniques and approaches to integrate individual analyses

- Attribute grammars
- (Declarative) (Datalog/Prolog) based approaches
- OPAL's fixed point computations framework

^ <!----------------------------------------------------------------------------------------------->
^ <!---------------------------------------- REFERENCES ------------------------------------------->
^ ---

^ # References

^ [^FPCF]: Lattice Based Modularization of Static Analyses; M. Eichberg, F. Kübler, D. Helm, M. Reif, G. Salvaneschi and M. Mezini; SOAP 2018, ACM
