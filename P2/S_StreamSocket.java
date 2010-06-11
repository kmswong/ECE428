/**
* S_StreamSocket.java
* Authors: John Huang, Kevin Lu, Ka Man Sophia Wong, Aaron Chun Win Yuen
* Course: ECE428
* Written in 2010
*/

import java.net.*;
import java.util.*;
import ece428.socket.*;

class S_StreamSocket
{
    /* Data members */
	private T_DatagramSocket m_socket;
	private final int PACKET_SIZE = 1000; // The packet size in bytes

    /* Constructor. Binds socket to addr */
    public S_StreamSocket(InetSocketAddress addr) throws SocketException
    {
		m_socket = new T_DatagramSocket(addr);
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

    /* Used by client to connect to server */
    public void S_connect(InetSocketAddress serverAddr) /* throws ... */
    {
		/* Your code here */
		
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
