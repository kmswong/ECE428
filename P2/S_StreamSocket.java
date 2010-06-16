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
	private final int CHUNK_SIZE = 1000; // The chunk size in bytes
	private final int RECEIVE_PACKET_SIZE = 1200;
	private InetSocketAddress m_toAddr;
	private final int START_SEQ_NUM = 0;
	private final int MAX_SEND_ATTEMPTS = 10; 

    /* Constructor. Binds socket to addr */
    public S_StreamSocket(InetSocketAddress addr) throws SocketException
    {
		m_socket = new T_DatagramSocket(addr);
		m_toAddr = null;
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
    public void S_connect(InetSocketAddress serverAddr) throws IOException
    {
		m_toAddr = serverAddr;
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
    public InetSocketAddress S_accept() throws IOException
    {
		// Receive first packet from client and get InetSocketAddress from data received
		byte[] buffer = new byte[1000];
		int size = S_receive(buffer, 0);
		InetSocketAddress addr = (InetSocketAddress) bytesToObject(buffer);
		m_toAddr = addr;
		
		// Send acknowledgement
		S_send(null, 0);
		
		// Receive to establish 3-way handshake
		S_receive(null, 0);
		
		return addr;
    }

    /* Used to send data. len can be arbitrarily large or small */
    public void S_send(byte[] buf, int len) throws IOException
    {	
		// Make Header
		int id = 0;
		int state = S_StreamPacket.STATE_SYN;
		int seq = 0;
		int ack = 0;
		int checksum = 0;
		
		int buff_index = 0;
		
		// While list != empty
		while (true) {
			// make a chunk
			byte [] chunk = new byte [CHUNK_SIZE];
			// copy data from buff to chunk
			for (int i = 0; i < CHUNK_SIZE || buff_index < len; i++)
			{
				chunk[i] = buf[buff_index++];
			}
			
			// make packet
			S_StreamPacket packet = new S_StreamPacket(id, state, seq, ack, checksum, chunk, (buff_index < len - 1) );
			
			// for 1 .. 10
			for (int i = 0; i < MAX_SEND_ATTEMPTS; i++)
			{
				// send packet using underlying UDP interface
				byte[] packet_bytes = objectToBytes(packet); 
				m_socket.T_sendto(packet_bytes, packet_bytes.length, m_toAddr);
				
				// result =s_receive()
				byte[] result = new byte[1200];
				
				// TODO: REFACTOR, BIOTCH!
				if (S_receive(result, len) == -1) break;
			}
		}
    }

    /* Used to receive data. Max chunk of data received is len. 
     * The actual number of bytes received is returned */
    public int S_receive(byte[] buf, int len) /* throws ... */
    {
		int curIndex = 0;
		while (true) {
			try {
					// receive packet
					DatagramPacket packet = m_socket.T_recvfrom(RECEIVE_PACKET_SIZE);

					// deserialize
					S_StreamPacket streamPacket = (S_StreamPacket)bytesToObject(packet.getData());

					// get data
					int minLen = Math.min(len-curIndex, streamPacket.getData().length);
					System.arraycopy(streamPacket.getData(), 0, buf, curIndex, minLen);
					curIndex += minLen;

					// send the ack packet to the sender
					S_StreamPacket ackPacket;
					if (curIndex < len) {
						ackPacket = new S_StreamPacket(0, S_StreamPacket.STATE_DFT, streamPacket.getAcknowledgementNumber(), streamPacket.getSequenceNumber() + curIndex, -1, null, false); 
					} else {
						System.err.println("ERROR IN S_RECEIVE");	
						ackPacket = new S_StreamPacket(0, S_StreamPacket.STATE_DFT, streamPacket.getAcknowledgementNumber(), streamPacket.getSequenceNumber() + curIndex, -1, null, false); 
					}

					byte[] ackPacketBytes = objectToBytes(ackPacket);
					m_socket.T_sendto(ackPacketBytes, ackPacketBytes.length, m_toAddr);

					// check if there is any more data
					if (!streamPacket.getMP()) break;
			} catch (SocketTimeoutException e) {
				//time out
			} catch (Exception e) {
			}
		}

		return curIndex;
    }

    /* To close the connection */
    public void S_close() /* throws ... */
    {
		m_socket.T_close();
    }
}
