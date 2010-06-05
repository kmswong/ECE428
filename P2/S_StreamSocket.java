import java.net.*;
import java.util.*;
import ece428.socket.*;

class S_StreamSocket
{
    /* Data members */

    /* Constructor. Binds socket to addr */
    public S_StreamSocket(InetSocketAddress addr) /* throws ... */
    {
	/* Your code here */
    }

    /* Receive timeout in milliseconds */
    public void S_setSoTimeout(int timeout) /* throws ... */
    {
	/* Your code here */
    }

    /* Details of local socket (IP & port) */
    public InetSocketAddress S_getLocalSocketAddress() /* throws ... */
    {
	/* Your code here */
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
	/* Your code here */
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
    }

    /* To close the connection */
    public void S_close() /* throws ... */
    {
	/* Your code here */
    }
}

