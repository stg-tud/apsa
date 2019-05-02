theme: APSA Lecture
autoscale: true
slidenumbers: true

# The Static Analysis Framework OPAL  

## The 3-Address Code Representation

Software Technology Group  
Department of Computer Science  
Technische Universität Darmstadt  
[Dr. Michael Eichberg](mailto:eichberg@informatik.tu-darmstadt.de)

---

# Getting the naive three-address code

```scala
object TACNaive {
  def apply(
    method:         Method,
    classHierarchy: ClassHierarchy,
    optimizations:  List[TACOptimization[Param, IdBasedVar]] = List.empty
  ): TACode[Param, IdBasedVar] = { ...}
}
```

^ Getting the naive three address code is always possible and does not require any kind of specialized data-flow analysis. However, only very few optimizations/transformations are performed.

^ In the very vast majority of cases it highly recommended to build your analysis on top of OPAL's SSA-like three-address code presented next.

---

# Getting the SSA-Like three-address code (TAC(AI))

^ This requires the execution of a data-flow analysis because the SAA-like address code is parameterized over the result of the data-flow analysis (`AIResult`).

```scala
object TACAI{
  def apply(
    project:  SomeProject,
    method:   Method,
    aiResult: AIResult { val domain: Domain with RecordDefUse }
  ): TACode[TACMethodParameter, DUVar[aiResult.domain.DomainValue]] = { ... }
}
```

A convenient way to get the SSA-like representation is to use one of the respective `ProjectInformationKey`s.

```scala
val tacaiKey = project.get(ComputeTACAIKey) // The Key: ComputedTACAIKey
val taCode = tacaiKey(m)
```

^ The `ComputeTACAIKey` returns a function object that – given a method – (re)computes the three-address code for the method on demand. I.e., the result is not cached. The link to the results of the underlying data-flow analysis are kept. The latter requires significant memory but generally provides more information.

^ The `Lazy|EagerDetachedTACAIKey` return function objects that compute the three-address code for a method lazily/eagerly and then detaches it from the results of the underlying data-flow analysis.


--- 

# Analysis specific adaptation of (TAC(AI))

^ The data-flow analysis that is used as a foundation for generating the SSA-like three address code can be changed by updating the information about the so-called domain that should be used. A domain basically determines how the values are taken into account.

```scala
project.updateProjectInformationKeyInitializationData(
    ComputeTACAIKey) { // or Lazy|EagerDetachedTACAIKey
    _ ⇒ (method: Method) ⇒ new DefaultDomainWithCFGAndDefUse(project, method) 
}
```

Alternative domains:

 - `l0.PrimitiveTACAIDomain` - only basic type information is propagated.  
 - `l1.DefaultDomainWithCFGAndDefUse` - type information is computed more precisely. 
 - `l2.DefaultPerformInvocationsDomainWithCFGAndDefUse` - monomorphic calls are inlined (depth:1).

^ The l0 domain is the fast domain that can be used. Depending on the use-case it may be sufficient and it provides SSA-like three-address code that is widely comparable to SSA code used by other frameworks.
 
^ The l1 domain additionally tracks `Class` and `String` objects (intra-procedurally) and tries to compute the ranges of integer variables.

^ Using the l2 domain makes it possible to, e.g., identify that the receiver objects of call chains such as `myStringBuilder.append(x).append(y)...` is actually always the same object.


---

# TACode 

The `TACode` object is the general entry point.

```scala
class TACode[P <: AnyRef, V <: Var[V]](
  val params:            Parameters[P],
  val stmts:             Array[Stmt[V]],
  val pcToIndex:         Array[Int],
  val cfg:               CFG[Stmt[V], TACStmts[V]],
  val exceptionHandlers: ExceptionHandlers,
  val lineNumberTable:   Option[LineNumberTable]
)
```

^ - `params` provides information about the parameters passed to the method; in particular about their use-sites.

^ - `stmts` contains the three-address code statements. (Manipulation is strictly prohibited.)

^ - `pcToIndex` contains the mapping between the pcs of the original bytecode instructions and the corresponding statements. It may be the case that multiple pcs are mapped to a single TAC statement. This data-structure is required if you want to use further code attributes which are not rewritten when we generate the `TACode` and, hence, need to be updated on-demand.

^ - `cfg` the control-flow graph. 

^ - `exceptionHandlers` specifies the try-catch blocks and handlers in terms of TAC statements.

^ - `lineNumberTable` a mapping of TAC statements to source line numbers.


--- 
# OPAL's three-address code 
## On the origin of values

When we analyze a method it may happen that a single expression/statement gives rise to different values: the value that is computed if the expression completes successfully and the value that is computed if the evaluation throws an exception!

In general, a def site can be a value in the ranges:

| Range | Semantics |
| ----- | ----- | 
| [0...`stmts.length`] | The `Assignment` statement at the given index (def-site) initialized the variable. |
| [-256...-1] | Identifies the respective parameter. |
| [-165535...-100,000] | Identifies an exception that was created by the JVM because the evaluation of the instruction failed. |

---

# OPAL's three-address code - Assignments

The most frequent statement is the `Assignment` statement:

```scala
case class Assignment[+V <: Var[V]](
  pc:        PC,
  targetVar: V,
  expr:      Expr[V]
)
```

^ `pc` is the pc of the underlying original bytecode instruction!

^ `targetVar` identifies the var which stores the result of the evaluation of the right-hand side's expression. In case of the TACAI based representation it is always a so-called `DVar`.

^ After generation, OPAL's three-address code is flat. That is, all expressions referred to by expressions are either `Var`s or `Consts`, but not further nested expressions. For example, if the right hand side is a binary expression then the operands are guaranteed to be either `Const`s or `Var`s.


---

# OPAL's three-address code - Unconditional jumps

Unconditional jumps:

```scala
case class Goto(pc: PC, target: Int) 
```

^ The target is the absolute address of the jump target in the statements array. 

^ If you analyze pre Java-6 code, you may encounter the following statements which are used by old compilers when compiling try-finally statements:
^ `case class JSR(pc: PC, target: Int)`
^ `case class Ret(pc: PC, returnAddresses: PCs)`

---

# OPAL's three-address code - Conditional jumps
  
```scala  
case class If[+V <: Var[V]](
  pc:            PC,
  left:          Expr[V],
  condition:     RelationalOperator,
  right:         Expr[V],
  target:        Int)
```

```scala
case class Switch[+V <: Var[V]](
  pc:            PC,
  defaultTarget: Int,
  index:         Expr[V],
  npairs:        RefArray[IntIntPair])
```

^ The target is always given as an absolute address.

^ In case of switches the `IntIntPair`'s first value is the case value; the second value is the absolute jump target.

---

# OPAL's three-address code - Normal return from method

case class ReturnValue[+V <: Var[V]](pc: Int, expr: Expr[V])
case class Return(pc: PC)

---

# OPAL's three-address code - Handling exceptions

```scala
case class Throw[+V <: Var[V]](pc: PC, exception: Expr[V])

case class CaughtException[+V <: Var[V]](
  pc:            PC,
  exceptionType: Option[ObjectType],
  throwingStmts: IntTrieSet
) 
```

^ If the `exception` is `null` a new instance of a `NullPointerException` is generated and thrown, in general, however, the `exception` expression is a variable.

^ In case of the TACAI based representation OPAL makes it explicit if an exception is caught by adding a `CaughtException` statement before the handler statement. 


---

# OPAL's three-address code - Method invocations

```scala
case class (Non)VirtualMethodCall[+V <: Var[V]](
        pc:             Int,
        declaringClass: ReferenceType,
        isInterface:    Boolean,
        name:           String,
        descriptor:     MethodDescriptor,
        receiver:       Expr[V],
        params:         Seq[Expr[V]])
```	

```scala
case class StaticMethodCall[+V <: Var[V]](
        pc:             Int,
        declaringClass: ObjectType,
        isInterface:    Boolean,
        name:           String,
        descriptor:     MethodDescriptor,
        params:         Seq[Expr[V]])
```

^ The parameters are specified in... in case of a virtual parameter the first parameter is always the self-reference `this`.

^ Given that it is possible to also call all methods defined by `java.lang.Object` on arrays the declaring class of virtual method calls can either be a class type or an array type.

^ A non-virtual instance method call is a call where the call target is statically resolved. Such a call is either the call of a private method, a super call or a constructor call.

^ Note that Java interfaces can now also define static and/or private methods.

---

# OPAL's three-address code - Writing fields

```scala
case class PutField[+V <: Var[V]](
        pc:                Int,
        declaringClass:    ObjectType,
        name:              String,
        declaredFieldType: FieldType,
        objRef:            Expr[V],
        value:             Expr[V]) 
```

```scala
case class PutStatic[+V <: Var[V]](
        pc:                PC,
        declaringClass:    ObjectType,
        name:              String,
        declaredFieldType: FieldType,
        value:             Expr[V])
```


---

```scala
case class InvokedynamicMethodCall[+V <: Var[V]](
        pc:              PC,
        bootstrapMethod: BootstrapMethod,
        name:            String,
        descriptor:      MethodDescriptor,
        params:          Seq[Expr[V]]
)
```

^ In general, it is recommended to let OPAL resolve `invokedynamic` based calls to avoid that analyses have to handle them explicitly. However, OPAL only provides resolution of Java/Scala `invokedynamic` calls at the moment. Therefore, it is always required to also be able to handle this statement.

---

# OPAL's three-address code - Statements

To facilitate an efficient conversion, OPAL sometimes inserts `NOP`s in the generated code.

```scala
Nop(pc: PC) 
```

case class ExprStmt[+V <: Var[V]](pc: Int, expr: Expr[V])

case class Checkcast[+V <: Var[V]](pc: PC, value: Expr[V], cmpTpe: ReferenceType)


```scala
case class ArrayStore[+V <: Var[V]](
        pc:       PC,
        arrayRef: Expr[V],
        index:    Expr[V],
        value:    Expr[V]
)
```

case class MonitorEnter[+V <: Var[V]](pc: PC, objRef: Expr[V])
case class MonitorExit[+V <: Var[V]](pc: PC, objRef: Expr[V])







