theme: APSA Lecture
autoscale: true
slidenumbers: true

# Applied Static Analysis

## An Introduction to Points-to and Alias Analysis

Software Technology Group  
Department of Computer Science  
Technische Universität Darmstadt  
[Dr. Michael Eichberg](mailto:m.eichberg@me.com)

> If you find any issues, please directly report them: [GitHub](https://github.com/stg-tud/apsa/blob/master/2019/8-IntroductionToPointsToAndAliasAnalysis/IntroductionToPointsToAndAliasAnalysis.md)

Some of the images on the following slides are inspired by slides created by Eric Bodden.

---

# Points-to analysis vs. alias analysis

- Points-to analysis computes for each variable the allocation sites whose objects the variable _may_/_must_ point to: $$ \text{points-to}(v) = \{ a1, a2, \dots \}$$

- Alias analysis determines which variables _may_ or _must_ alias, i.e., point to the same objects:  
  - $$\text{may-alias}(v1,v2) = true/false$$  
  - $$\text{must-alias}(v1,v2) = true/false$$


^ In case of a _may_ analysis $true$ means **maybe**. I.e., if two variables may alias then they may point to the same object, but they don't have to. If the answer is $false$, they definitively never alias.
^ In case of a _must_ analysis $false$ (only) means _maybe not_. 

---

# May vs. must alias analysis

```java
a = new A();
if(..) {
	b = a;
}
c = new C();
d = c;
```

^ $$\text{may-alias}(a,b)  = true$$ 
 
^ $$\text{must-alias}(a,b) = false$$ 
 
^ $$\text{may-alias}(a,c)  = false$$  
 
^ $$\text{must-alias}(c,d) = true$$ 

---

# May vs. must alias analysis

```java
a = new A();
if(..) {
	b = a;
}
c = new C();
d = c;
d = a;  // <= NEW
```

^ $$\text{may-alias}(a,b)  = true$$ 
 
^ $$\text{must-alias}(a,b) = false$$ 
 
^ $$\text{may-alias}(a,c)  = false$$  
 
^ $$\text{must-alias}(c,d) = false$$ 

^ <!----------------------------------------------------------------------------------------------->
^ <!---------------------------------------- REFERENCES ------------------------------------------->
^ ---

^ # References

^ [^DVTA]: Practical Virtual Method Call Resolution for Java; Vijay Sundaresan, Laurie Hendren, Chrislain Razafimahefa, Raja Valleé-Rai, Patrick Lam, Etienne Gagnon and Charles Godin; OOPSLA 2000, ACM
