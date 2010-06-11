/**
* S_StreamSocket.java
* Authors: John Huang, Kevin Lu, Ka Man Sophia Wong, Aaron Chun Win Yuen
* Course: ECE428
* Written in 2010
*/

import java.io.*;
import java.net.*;
import java.util.*;
import ece428.socket.*;

class S_StreamSocket
{
    /* Data members */
	private T_DatagramSocket m_socket;
	private final int m_packetSize = 1000; // The packet size in bytes
	private InetSocketAddress toAddr;

    /* Constructor. Binds socket to addr */
    public S_StreamSocket(InetSocketAddress addr) throws SocketException
    {
		m_socket = new T_DatagramSocket(addr);
		toAddr = null;
    }

    /* Receive timeout in milliseconds */
    public void S_setSoTimeout(int timeout) throws SocketException
    {
		m_socket.T_setSoTimeout(timeout);
    }

    /* Details of local socket (IP & port) */
    public InetSocketAddress S_getLocalSocketAddress() /* throws ... */
    {
		return m_socket.T_getLocalSocketAddress();
    }

	private byte[] objectToBytes(Object o) {
		byte[] result = null;
		try {
			ByteArrayOutputStream bs = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(bs);
			os.writeObject(o);
			os.flush();
			os.close();
			bs.close();
			result = bs.toByteArray();
		} catch (Exception e) {
		
		}
		return result;
	}

	private Object bytesToObject(byte[] bytes){
		Object result = null;
		try {
			ByteArrayInputStream bs = new ByteArrayInputStream(bytes);
			ObjectInputStream os = new ObjectInputStream(bs);
			result = os.readObject();
		} catch (Exception e) {
		}

		return result;
	}

    /* Used by client to connect to server */
    public void S_connect(InetSocketAddress serverAddr) /* throws ... */
    {
		toAddr = serverAddr;
		byte[] data = objectToBytes(serverAddr);

		//send the sync packet
		S_send(data, data.length);	

		//receive the ack packet	
		S_receive(null, 0);

		//send the ack packet
		S_send(null, 0);
    }

    /* Used by server to accept a new connection */
    /* Returns the IP & port of the client */
    public InetSocketAddress S_accept() /* throws ... */
    {
		// Receive first packet from client
		S_receive(null, 0);
		
		// Get InetSocketAddress from data received
		
		// Send acknowledgement
		S_send(null, 0);
		
		// Receive to establish 3-way handshake
		S_receive(null, 0);
		
		return null;
    }

    /* Used to send data. len can be arbitrarily large or small */
    public void S_send(byte[] buf, int len) /* throws ... */
    {
		/* Your code here */
    }

    /* Used to receive data. Max chunk of data received is len. 
     * The actual number of bytes received is returned */
    public int S_receive(byte[] buf, int len) /* throws ... */
    {
		/* Your code here */
		return 0;
    }

    /* To close the connection */
    public void S_close() /* throws ... */
    {
		m_socket.T_close();
    }
}
