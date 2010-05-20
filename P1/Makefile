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

TCP_CLASSES = \
	clientTCP.java \
	serverTCP.java \

UDP_CLASSES = \
	clientUDP.java \
	serverUDP.java \

default: classes

classes: $(CLASSES:.java=.class)
runTCP: $(TCP_CLASSES:.java=.class)
	java serverTCP &
	sleep 1
	java clientTCP $(F) $(C)
runUDP: $(UDP_CLASSES:.java=.class)
	java serverUDP &
	sleep 1
	java clientUDP $(F) $(C)

clean:
	$(RM) *.class
