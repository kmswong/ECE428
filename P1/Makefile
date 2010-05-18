GS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
        $(JC) $(JFLAGS) $*.java

CLASSES = \
        clientTCP.java \
        serverTCP.java \
        clientUDP.java \
        serverUDP.java \

default: classes

classes: $(CLASSES:.java=.class)
runTCP: $(CLASSES:.java=.class)
runUDP: $(CLASSES:.java=.class)

clean:
        $(RM) *.class
