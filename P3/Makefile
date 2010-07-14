JFLAGS = -g -1.6
JC = javac
CLASSPATH = ./
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) -cp $(CLASSPATH) $*.java

CLASSES = \
	CombinationGenerator.java \
	p3server.java 

default: classes

classes: $(CLASSES:.java=.class)

run1: classes
	java p3server 0 0

run2: classes
	java p3server $(F) $(N)

run3: classes
	java p3server 0 1

clean:
	$(RM) *.class
