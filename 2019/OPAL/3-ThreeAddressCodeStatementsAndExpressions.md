theme: APSA Lecture
autoscale: true
slidenumbers: true

# The Static Analysis Framework OPAL  

## The 3-Address Code Representation
**Statements and Expression**

Software Technology Group  
Department of Computer Science  
Technische Universität Darmstadt  
[Dr. Michael Eichberg](mailto:m.eichberg@me.com)


> If you have questions don't hesitate to join our public chat:   [Gitter](https://gitter.im/OPAL-Project/Lobby)
> If you find any issues, please directly report them: [GitHub](https://github.com/stg-tud/apsa/blob/master/2019/OPAL/2-AnalyzingThreeAddressCode.md)


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


case class ExprStmt[+V <: Var[V]](pc: Int, expr: Expr[V])

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

```scala
case class ReturnValue[+V <: Var[V]](pc: Int, expr: Expr[V])
case class Return(pc: PC)
```

^ Recall that a return statement may throw an exception!

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

# OPAL's three-address code - Invokedynamic

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

# OPAL's three-address code - Checkcast

```scala
case class Checkcast[+V <: Var[V]](
  pc: PC,
  value: Expr[V],
  cmpTpe: ReferenceType
)
```


---

# OPAL's three-address code - Array writes

```scala
case class ArrayStore[+V <: Var[V]](
  pc:       PC,
  arrayRef: Expr[V],
  index:    Expr[V],
  value:    Expr[V]
)
```


---

# OPAL's three-address code - Synchronization

```scala
case class MonitorEnter[+V <: Var[V]](pc: PC, objRef: Expr[V])
case class MonitorExit[+V <: Var[V]](pc: PC, objRef: Expr[V])
```

^ `MonitorEnter` and `MonitorExit` statements w.r.t. a specific object always need to occur inside one method and need to be balanced.

---

# OPAL's three-address code - Nops

To facilitate an efficient conversion, OPAL sometimes inserts `NOP`s in the generated code.

```scala
case class Nop(pc: PC) 
```






