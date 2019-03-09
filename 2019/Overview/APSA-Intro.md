autoscale: true
slidenumbers: true

# Applied Static Analysis
Software Technology Group  
Dr. Michael Eichberg

Summer Term 2019

---

# Lecture
Every week at ... in ....

The theoretical concepts generally apply to a very wide range of languages and programming language paradigms; however, in the examples etc. we are primarily concerned with the analysis of Java (Bytecode). The language that we use for implementing analyses is Scala[^Scala].

The lecture slides can be found at: [https://github.com/stg-tud/apsa](https://github.com/stg-tud/apsa)

^ Let's see what happens...
dfsflj
adfasdfs
asdfsf

---

# Exam
We will have a closed-book exam after the semester.  
The exam will be 60 minutes.

---

# Exercise
Every week we will have ~60minutes of lecture and ~30minutes exercises. 

Some exercise will just be theory and some will require you to comprehend and write concrete static analyses. The analyses will be developed using the [OPAL](www.opal-project.de) framework.

The exercises will help you to prepare for the exam.

---

# Planned Content

* Basic terminology (e.g., soundness, precision, context-sensitivity, ...)
* Code representations (e.g., three-address code)
* Parallelization of static analyses[^ReactiveAsync] 
* Call-graph construction
* Inter-procedural data-flow analyses (IFDS, IDE, Weighted Pushdown Systems)
* Purity and immutability analysis
* Escape analysis/Points-to analysis
* Code slicing


$$\sum_x^1 \mathcal{I}$$


---


[.code-highlight: 2]

~~~scala
case class X {
	def foo(){
	}
}
~~~

```java
class X {
	def foo(){
	}
}
```

---

# References



[^Scala]: [Scala](www.scala-lang.org)

[^ReactiveAsync]: Reactive Async, Haller et al. 2017

