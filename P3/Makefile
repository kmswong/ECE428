JFLAGS = -g -1.6
JC = javac
CLASSPATH = ./
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) -cp $(CLASSPATH) $*.java

CLASSES = \
	p3server.java 

default: classes

classes: $(CLASSES:.java=.class)

run1: classes
	java p3server 1 1

run2: classes
	java p3server $(F) $(N)

clean:
	$(RM) *.class
