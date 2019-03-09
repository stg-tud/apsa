autoscale: true
slidenumbers: true

# [fit] Applied Static Analysis  

Software Technology Group 
Technische Universität Darmstadt
__*Dr. Michael Eichberg*__

Summer Term 2019





---

# Lecture
Every week at 11:40am in S101|A03,

The theoretical concepts generally apply to a very wide range of languages and programming language paradigms; however, in the examples etc. we will primarily analyze Java (Bytecode). The language that we use for implementing analyses is [Scala](https://www.scala-lang.org)).

The lecture slides can be found at: [https://github.com/stg-tud/apsa](https://github.com/stg-tud/apsa/tree/master/2019)

^ The lecture slides are basically markdown files that are presented using [Deskset](https://www.deskset.com). The final PDF is rendered using [Marked 2](https://marked2app.com) after preprocessing them using the following [sed](https://en.wikipedia.org/wiki/Sed) program:
```bash
/usr/bin/sed -E -e 's/^\[\..+\:.+\]//' -e 's/^\^[[:space:]]//' -e 's/#[[:space:]]\[fit\]/#/'
```
The program basically removes all non-standard markdown commands required by Deskset.
Hence, if you find typos or more significant issues don't hesitate to create [issues](https://github.com/stg-tud/apsa/issues). Pull requests are also very welcome. 

---

# Exam

- We will have a closed-book exam after the semester.  
- The exam will be 60 minutes.
- (There will be no bonus.)

---

# Exercise
Every week we will have ~60minutes of lecture and ~30minutes exercises. 

Some exercise will just be theory and some will require you to comprehend and write concrete static analyses. The analyses will be developed using the [OPAL](www.opal-project.de) framework.

The exercises will help you to prepare for the exam.

---

# Planned Content

* Basic terminology (e.g., soundness, precision, context-sensitivity, ...)
* Code representations (e.g., three-address code)
* Parallelization of static analyses
* Call-graph construction
* Inter-procedural data-flow analyses (IFDS, IDE, Weighted Pushdown Systems)
* Purity and immutability analysis
* Escape analysis/Points-to analysis
* Code slicing

--- 

# Prerequisites

 * A keen interest in analyzing code.
 * Basic knowledge in compiler construction is helpful.
 * A very good understanding of object-oriented programming and in-particular of Java.
 * Willing to learn and use Scala.




