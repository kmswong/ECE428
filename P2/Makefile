JFLAGS = -g
JC = javac
CLASSPATH = ./
.SUFFIXES: .java .class
.java.class:
		$(JC) $(JFLAGS) -cp $(CLASSPATH) $*.java

CLASSES = \
		ece428/socket/T_DatagramSocket.java \
		S_StreamPacket.java \
		S_StreamSocket.java \
		clientTCP.java \
		serverTCP.java \
		clientTCPOneWay.java \
		serverTCPOneWay.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
		$(RM) *.class
		$(RM) ece428/socket/*.class
