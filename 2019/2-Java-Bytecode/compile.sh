javac *.java
java -jar /Users/Michael/Code/OPAL/OPAL/da/target/scala-2.12/OPALDisassembler.jar Constants -o Constants.html
java -jar /Users/Michael/Code/OPAL/OPAL/da/target/scala-2.12/OPALDisassembler.jar ControlFlow -o ControlFlow.html
java -jar /Users/Michael/Code/OPAL/OPAL/da/target/scala-2.12/OPALDisassembler.jar ImmutableStore -o ImmutableStore.html
java -jar /Users/Michael/Code/OPAL/OPAL/da/target/scala-2.12/OPALDisassembler.jar Operators -o Operators.html
java -jar /Users/Michael/Code/OPAL/OPAL/da/target/scala-2.12/OPALDisassembler.jar SynchronizedStore -o SynchronizedStore.html

