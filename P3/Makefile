JFLAGS = -g
JC = javac
P = 1 
CLASSPATH = ./
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) -cp $(CLASSPATH) $*.java

CLASSES = \
	p3server.java 

default: classes

classes: $(CLASSES:.java=.class)

run1: classes
	java p3server &
	sleep 1
	/home/tripunit/p3client -s $(P) -f 1 -n 1

run2: classes
	java p3server &
	sleep 1
	/home/tripunit/p3client -s $(P) -f $(F) -n $(N)

clean:
	$(RM) *.class
