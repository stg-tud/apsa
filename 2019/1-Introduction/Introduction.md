autoscale: true
slidenumbers: true

# Applied Static Analysis

## Static Analysis (Tools)


Software Technology Group 
Technische Universität Darmstadt
[Dr. Michael Eichberg](mailto:eichberg@informatik.tu-darmstadt.de)

---

# What is Static Analysis?

...


---

# Purposes

[.build-lists: true]

- Finding bugs.
- Quality assessments.
- Improving the quality of the code (e.g. by removing code clones).
- Optimizing the code.

^ When we are concerned with bugs, we have to distinguish

^ - To asses the quality of some software or to help refactor the software
(e.g., by applying metrics or by visualizing the structure)
^ - To optimize the code.
(e.g., by removing dead code, by hoisting loop invariants, by removing useless synchronization, by avoiding runtime checks to detect `null-pointer exceptions` or `array index out of bound exceptions`.)

---


# Finding Programming Bugs

[.code-highlight: 5]

```java
class X {
	private long scale;
	X(long scale) { this.scale = scale; }
	void adapt(BigDecimal d){
		d.setScale(this.scale);
	}
}
```

[.build-lists: true]

There is always more than one way to find certains bugs!

^ In this case the bug is that the `setScale` method is side-effect free (though the name `setScale` suggests something different) and therefore the call `d.setScale(...)` is useless; most likely buggy.

^ > [JavaDoc of java.lang.BigDecimal.setScale](https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html#setScale-int-):   
[...] Note that since BigDecimal objects are immutable, calls of this method do not result in the original object being modified, contrary to the usual convention of having methods named setX mutate field X. Instead, setScale returns an object with the proper scale; the returned object may or may not be newly allocated.  

---

# Finding Bugs Using Bug Patterns

^ Identifying such bugs can be done using very simple – so called – bug patterns.



---

# Finding Code Smells (Bugs) Using General Purpose Static Analyses

[.code-highlight: 8]

```java
class com.sun.imageio.plugins.png.PNGMetadata{
	void mergeStandardTree(org.w3c.dom.Node) {
		[...]
		if (maxBits > 4 || maxBits < 8) {
    		maxBits = 8;
		}
		if (maxBits > 8) {
    		maxBits = 16;
		}
		[...]
	}
}
```

^ The condition of the first `if` (Line 4) will always be true. Every possible int value will either be larger than four or smaller than eight. Hence, Line 5 will **always** be executed and therefore the condition of the second `if` will never be `true`.

---

# Finding Code Smells (Bugs) 
## Using General Purpose Static Analyses

[.code-highlight: 4]

```java
class sun.font.StandardGlyphVector {
	public int getGlyphCharIndex(int ix) {
		if (ix < 0 && ix >= glyphs.length) {
			throw new IndexOutOfBoundsException("" + ix);
		}
	}
}
```

^ The condition (Line 3) will never be `true`; `glyphs.length` will either be a value equal or larger than `0` or will throw a `NullPointerException`. Hence, there is no effective validation of the parameter `ix`.

---

# Finding Code Smells (Bugs)
## Using General Purpose Static Analyses

```java
class sun.tracing.MultiplexProviderFactory {
	public void uncheckedTrigger(Object[] args) {
		[...]
		Method m = Probe.class.getMethod(
			"trigger", Class.forName("[java.lang.Object"));
		m.invoke(p, args);
	}
}
```

^ The `m.invoke` statement will never be executed, because the string passed to `Class.forName` is invalid.

---

# Finding Code Smells (Bugs)
## Using General Purpose Static Analyses

```java
class com.sun.corba.se.impl.naming.pcosnaming.NamingContextImpl {
	public static String nameToString(NameComponent[] name)
		[...]
		if (name != null || name.length > 0) {
			[...]
		}
		[...]
	}
}
```

^ If `name` is `null` the test `name.length > 0` (Line 4) will be executed and will result in a `NullPointerException`. In this case, we have confused logical operators. The developer most likely wanted to use `&&`.

---


# Finding Bugs Via Method Protocol Violations

*By finding contradicting code snippets*[^Pradel]

```javascript
...
```

---


# Finding Bugs Using Machine Learning

*By finding contradicting code snippets*[^Pradel]

```javascript
...
```

---

# Finding Bugs Using Specialized Static Analyses

**[^Cognicrypt]



---

# References

[^Pradel]: Michael Pradel