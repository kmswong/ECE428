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
	private InetSocketAddress m_toAddr;
	private int m_state;
	private final int CHUNK_SIZE = 1000; // The chunk size in bytes
	private final int RECEIVE_PACKET_SIZE = 1200;
	private final int START_SEQ_NUM = 0;
	private final int MAX_SEND_ATTEMPTS = 10; 
	private final String IP = "127.0.0.1";
	
	public static final int STATE_CON = 0;
	public static final int STATE_SYN = 1;
	public static final int STATE_ACK = 2;
	public static final int STATE_CLD = 3;

    /* Constructor. Binds socket to addr */
    public S_StreamSocket(InetSocketAddress addr) throws SocketException
    {
		m_socket = new T_DatagramSocket(addr);
		m_toAddr = null;
		m_state = S_StreamSocket.STATE_CLD;
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
		ObjectOutputStream os = null;
		ByteArrayOutputStream bs = null;
		try {
			bs = new ByteArrayOutputStream();
			os = new ObjectOutputStream(bs);
			os.writeObject(o);
			os.flush();
			os.close();
			bs.close();
			
			result = bs.toByteArray();
		} catch (IOException e) {
			System.err.println("ERROR1: " + e.getMessage());
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
		
		InetSocketAddress selfAddr = m_socket.T_getLocalSocketAddress();
		String ip = selfAddr.getHostName();
		System.out.println("host name: " + ip);
		int port = selfAddr.getPort();
		if (ip.equals("0.0.0.0")) {
			ip = IP;
		}
		InetSocketAddress replyAddr = new InetSocketAddress(ip, port); 
		byte[] data = objectToBytes(replyAddr);

		m_state = S_StreamSocket.STATE_SYN;
		//send the sync packet
		System.out.println("CLIENT: SEND SYN PACKET");
		S_send(data, data.length);	

		m_state = S_StreamSocket.STATE_ACK;
		//send the ack packet
		System.out.println("CLIENT: SEND ACK PACKET AGAIN");
		S_send(null, 0);
		
		m_state = S_StreamSocket.STATE_CON;
		throw new IOException();
    }

    /* Used by server to accept a new connection */
    /* Returns the IP & port of the client */
    public InetSocketAddress S_accept() throws IOException
    {
		// Receive first packet from client and get InetSocketAddress from data received
		byte[] buffer = new byte[1000];
		System.out.println("SERVER: RECEIVE FIRST PACKET");
		
		m_state = S_StreamSocket.STATE_ACK;
		S_receive(buffer, 1000);

		// Receive to establish 3-way handshake
		System.out.println("SERVER: RECEVIE TO ESTABLISH 3WAY HANDSHAKE");
		S_receive(null, 0);
		
		m_state = S_StreamSocket.STATE_CON;
		throw new IOException();
		//return addr;
    }

    /* Used to send data. len can be arbitrarily large or small */
    public void S_send(byte[] buf, int len) throws IOException
    {
		// Make Header
		int id = 10;
		int seq = 0;
		int ack = 0;
		int checksum = 0;
		
		int buff_index = 0;
		int chunkSize = Math.min(CHUNK_SIZE, len);
		
		// While list != empty
		while (true) {
			// make a chunk
			byte [] chunk = new byte [chunkSize];
			// copy data from buff to chunk
			for (int i = 0; i < chunkSize && buff_index < len; i++)
			{
				chunk[i] = buf[buff_index++];
			}
			
			// make packet
			S_StreamPacket packet = new S_StreamPacket(id, m_state, seq, ack, checksum, chunk, (buff_index < len - 1) );
			
			// for 1 .. 10
			for (int i = 0; i < MAX_SEND_ATTEMPTS; i++)
			{
				//System.out.println("attempt: " + i);
			
				// send packet using underlying UDP interface
				byte[] packet_bytes = objectToBytes(packet); 
				
				System.out.println("sending to " + m_toAddr + " from " + m_socket.T_getLocalSocketAddress());
				m_socket.T_sendto(packet_bytes, packet_bytes.length, m_toAddr);
				
				// result =s_receive()
				byte[] result = new byte[1200];
				
				// receive ack packet
				DatagramPacket ackPacket = m_socket.T_recvfrom(RECEIVE_PACKET_SIZE);

				// deserialize
				S_StreamPacket streamPacket = (S_StreamPacket)bytesToObject(ackPacket.getData());
				System.out.println("ID of the ack packet: " + streamPacket.getId());
				
				// if the correct ack number is receive then this chunk is correctly received
				if (streamPacket.getAcknowledgementNumber() == buff_index || streamPacket.getState() == S_StreamSocket.STATE_ACK) break;
			}
			
			System.out.println("done sending one chunk");
			if (buff_index == len) break;
		}
		System.out.println("done sent");
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
					System.out.println("ID of the received packet: " + streamPacket.getId());
				
					
					// get data
					int minLen = Math.min(len-curIndex, streamPacket.getData().length);
					System.arraycopy(streamPacket.getData(), 0, buf, curIndex, minLen);
					curIndex += minLen;

					System.out.println("A");
					if (streamPacket.getState() == S_StreamSocket.STATE_SYN && m_toAddr == null && !streamPacket.getMP()) {
						InetSocketAddress addr = (InetSocketAddress) bytesToObject(buf);
						System.out.println("accepted addr: " + addr);
						m_toAddr = addr;
					}
					
					System.out.println("B");
					if (m_toAddr != null) {
						System.out.println("curIndex: " + curIndex + ", len " + len);
						// send the ack packet to the sender
						S_StreamPacket ackPacket;
						if (len == 0 || curIndex < len) {
							ackPacket = new S_StreamPacket(0, m_state, streamPacket.getAcknowledgementNumber(), streamPacket.getSequenceNumber() + curIndex, -1, null, false); 
						} else {
							System.err.println("ERROR IN S_RECEIVE");	
							ackPacket = new S_StreamPacket(0, m_state, streamPacket.getAcknowledgementNumber(), streamPacket.getSequenceNumber() + curIndex, -1, null, false); 
						}

						byte[] ackPacketBytes = objectToBytes(ackPacket);
						m_socket.T_sendto(ackPacketBytes, ackPacketBytes.length, m_toAddr);
						System.out.println("done sending back ack");
					}

					System.out.println("C");
					// check if there is any more data
					if (!streamPacket.getMP()) break;
			} catch (SocketTimeoutException e) {
				//time out
				System.err.println("RECEIVE TO ERROR: " + e.getMessage());
			} catch (Exception e) {
				System.err.println("RECEIVE ERROR: " + e.getMessage());
			}
		}
		System.out.println("done receive");
		return curIndex;
    }

    /* To close the connection */
    public void S_close() /* throws ... */
    {
		m_socket.T_close();
    }
}
