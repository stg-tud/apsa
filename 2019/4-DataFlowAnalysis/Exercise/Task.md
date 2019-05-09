# Applied Static Analysis

Software Technology Group  
Department of Computer Science  
Technische Universität Darmstadt  
[Dr. Michael Eichberg](mailto:m.eichberg@me.com)

# Monotone Framework

You should use `MyOPALProject` as a template. That project is preconfigured to use the latest snapshot version of OPAL. You can clone the project using:  
`git clone --depth 1 https://bitbucket.org/OPAL-Project/myopalproject Project`

> ️⚠️ Always ensure that you use the latest snapshot version. You can clean the latest (snapshot) version that you have downloaded using the command `sbt cleanCache cleanLocal` in your project's root folder.

An integrated JavaDoc of the latest snapshot version of OPAL that spans all sub-projects can be found at:
[www.opal-project.de](http://www.opal-project.de/library/api/SNAPSHOT)

For further details regarding the development of static analysis using OPAL see the OPAL tutorial.

You should develop the following analysis on top of the 3-address code representation (TACAI) offered by OPAL. Use the `l1.DefaultDomainWithCFGAndDefUse` domain and the `ProjectInformationKey` `ComputeTACAIKey` as the foundation for your analysis.


## Closeables

Develop an __intraprocedural__ analysis using the monotone framework which finds violations of the following rule taken from [The CERT Oracle Secure Coding Standard for Java](https://wiki.sei.cmu.edu/confluence/display/java):

> FIO04-J: Close resources when they are no longer needed.

Here, we consider as a resource every instance of the class `java.lang.AutoCloseable`.

Non-compliant example:
```java
public int processFile(String fileName) throws Exception {
  FileInputStream stream = new FileInputStream(fileName);
  BufferedReader bufRead = new BufferedReader(new InputStreamReader(stream));
  String line;
  while ((line = bufRead.readLine()) != null) {
    sendLine(line);
  }
  return 1;
}
```

Compliant example:
```java
try (FileInputStream stream = new FileInputStream(fileName);
     InputStreamReader reader = new InputStreamReader(stream);
     BufferedReader bufRead = new BufferedReader(reader)) {
  String line;
  while ((line = bufRead.readLine()) != null) {
    sendLine(line);
  }
} catch (IOException e) {
    log(e);
}
```

> Do not forget that this analysis requires that the class hierarchy is reasonably complete. Hence, you should load the JDK (at least) as a library: `run -cp=<Path>/Closeables.class -libcp=<PATH>/jdk1.8.0_191.jdk/Contents/Home/jre/lib/rt.jar`

To reduce the number of false positives, only apply this check if the resource object is created inside the same method and ...:

- neither passed to some other method, nor returned and also not stored in a field (i.e., only apply this check if the resource object is subject to garbage collection at the end of the method), **unless**
- the resource is never returned.

For example, ignore the following cases where the stream is passed in as a parameter:
```java
public int processFile(FileInputStream stream) throws Exception {
  BufferedReader bufRead = new BufferedReader(new InputStreamReader(stream));
  String line;
  while ((line = bufRead.readLine()) != null) {
    sendLine(line);
  }
  return 1;
}
```

**Hints**

 - To get the definition site(s) of the receiver of the method call to `close`, you can use the following pattern match:  
`case VirtualMethodCall(_, _, _, "close", NoArgsAndReturnVoid, receiver, _) ⇒ receiver.asVar.definedBy...`  
 _If you have multiple definition sites you can consider all resources as being closed. Hence, you should lean towards a precise analysis in this case; i.e., we may have false negatives. Getting a more sound and precise solution would require a more elaborate control- and data-flow analysis which is beyond the scope of this exercise!_

 - If calling the constructor throws an exception the resource is not to be considered as being initialized; no need to close it. That is, only after the `<init>` call the newly created resource should be considered initialized. Note that you have the guarantee that a newly created object is initialized at most once.

 - If you are analyzing an object that is `AutoCloseable` you should ignore the call of the super constructor. That is, it is correct that the constructor of an `AutoCloseable` object does not `close` itself.

 - As a first step design the lattice of your analysis. Recall that you should also identify the case, closed on some path


**Tasks**

 1. Test your analysis using the class `Closeables`. It should not produce any false positives.
 1. Run your analysis against the JDK.
