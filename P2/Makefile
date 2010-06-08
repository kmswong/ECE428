JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) $*.java

CLASSES = \
		ece428/socket/T_DatagramSocket.java \
		S_StreamSocket.java \

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class