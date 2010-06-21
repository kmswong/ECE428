/**
 * S_StreamSocket.java
 * Authors: John Huang, Kevin Lu, Ka Man Sophia Wong, Aaron Chun Win Yuen
 * Course: ECE428
 * Written in 2010
 */

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

import ece428.socket.*;

/**
 * This is the StreamSocket interface we have to implement for Project 2. External classes use this interface like using
 * a TCP interface to establish a TCP connection.
 */
class S_StreamSocket
{
    // Data members.
	private T_DatagramSocket m_socket;
	private InetSocketAddress m_toAddr;
	private int m_state;
	private int m_seq;
	private int m_ack;
	private S_StreamPacket m_ackPacket;
	private final int CHUNK_SIZE = 1000; // The chunk size in bytes
	private final int RECEIVE_PACKET_SIZE = 1200;
	private final int START_SEQ_NUM = 0;
	private final int MAX_SEND_ATTEMPTS = 50; 
	private final String IP = "127.0.0.1";
	
	public static final int STATE_CON = 0; // Connect
	public static final int STATE_SYN = 1; // Syn
	public static final int STATE_ACK = 2; // Ack
	public static final int STATE_CLD = 3; // Close
	public static final int STATE_ERR = 4; // Error (Checksum)
	
	private static final String DIGEST_ALGORITHM_NAME = "MD5";
	
	private final int TIMEOUT = 250;

    /** 
	 * Constructor. 
	 * 
	 * Args: 
	 *   addr       		The address that will be bounded to the socket.
	 *
	 * Throws:
	 *   SocketException
	 */
    public S_StreamSocket(InetSocketAddress addr) throws SocketException
    {
		m_socket = new T_DatagramSocket(addr);
		m_toAddr = null;
		m_state = S_StreamSocket.STATE_CLD;
		m_ack = 0;
		m_seq = 0;
		m_ackPacket = new S_StreamPacket(0, STATE_ERR, m_seq, m_ack, null, null, false);
    }

	/**
	 * Sets the socket timeout in milliseconds.
	 *
	 * Args:
	 * 	 timeout			The timeout of the socket to be set in milliseconds.
	 */
    public void S_setSoTimeout(int timeout) throws SocketException
    {
		m_socket.T_setSoTimeout(timeout);
    }

	/**
	 * Retrieves the local address as a InetSocketAddress. Details include IP and port.
	 */
    public InetSocketAddress S_getLocalSocketAddress() /* throws ... */
    {
		return m_socket.T_getLocalSocketAddress();
    }

	/**
	 * This method is used by the client to connect to the server.
	 *
	 * Args:
	 *   serverAddr			The server address to connect to.
	 *
	 * Throws:
	 *   IOException
	 */
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

		
		// Makes sure that the first handshake occurs.
		m_state = S_StreamSocket.STATE_SYN;
		//send the sync packet
		System.out.println("CLIENT: SEND SYN PACKET");
		S_send(data, data.length);	
		
		m_state = S_StreamSocket.STATE_ACK;
		//send the ack packet
		System.out.println("CLIENT: SEND ACK PACKET AGAIN");
		S_send(null, 0);
		
		m_state = S_StreamSocket.STATE_CON;
		System.out.println("done connect");
    }

	/**
	 * This method is used by the server to accept a connection from a client.
	 *
	 * Returns:
	 * 	 Returns the address of the client as an InetSocketAddress.
	 *
	 * Throws:
	 *   IOException
	 */
    public InetSocketAddress S_accept() throws IOException
    {
		// Receive first packet from client and get InetSocketAddress from data received
		byte[] buffer = new byte[1000];

		
		m_state = S_StreamSocket.STATE_ACK;
		S_receive(buffer, 1000);		
		System.out.println("SERVER: RECEIVED FIRST PACKET");

		// Receive to establish 3-way handshake
		S_receive(null, 0);
		System.out.println("SERVER: RECEIVE TO ESTABLISH 3WAY HANDSHAKE");
		
		m_state = S_StreamSocket.STATE_CON;

		System.out.println("done accept");
		return m_toAddr;
    }
	
	private byte[] calculatePacketChecksum( S_StreamPacket sp )
	{
		byte[] serializedStreamPacket = objectToBytes(sp);
		
		return calculateChecksum( serializedStreamPacket );
	}
	
	
	/**
	 * Send an array of bytes of data when a connection is established. Len can be arbitrarily large or small.
	 *
	 * Args:
	 *   buf				The array of data to send.
	 *   len				The length of the array of bytes.
	 *
	 * Throws:
	 *   IOException
	 */
    public void S_send(byte[] buf, int len) throws IOException
    {
		byte[] ackPacketData = null;
		byte[] packetChecksum = null;				
		
		int buff_index = 0;
		int chunkSize = Math.min(CHUNK_SIZE, len);
		byte [] chunk;
		
		// Stream Packet Info
		S_StreamPacket packet;
		int id = 10;
		byte[] checksum;
		byte[] packet_bytes;			// serialized version of packet
		
		// While list != empty
		while (true) {
			// make a chunk
			chunk = new byte [chunkSize];
			
			// Increase the Acknowledgement number
			m_ack += 1;
			
			// copy data from buff to chunk
			for (int i = 0; i < chunkSize && buff_index < len; i++)
			{
				chunk[i] = buf[buff_index++];
			}
			System.out.println(" WWEEEEEEEEEEE SENDING WITH SEQ: " + m_seq + " ACK: " + m_ack + "\n" );
			S_StreamPacket packet = new S_StreamPacket(id, m_state, m_seq, m_ack, null, chunk, (buff_index < len - 1) );
			checksum = calculateChecksum(packet.getData());	
			packet.setChecksum(checksum);
			System.out.print("Send checksum is " );
			for(int a = 0; a < checksum.length; a++) {
				System.out.print(checksum[a]);
			}
			System.out.println();
			
			byte[] packet_bytes = objectToBytes(packet); 
            System.out.println("number of packet bytes is " + packet_bytes.length);
			byte[] result = new byte[1200];
				

			int i;
			// for 1 .. 10
			for ( i = 0; i < MAX_SEND_ATTEMPTS; i++)
			{
				// send packet using underlying UDP interface			
				packet = new S_StreamPacket(id, m_state, m_seq, m_ack, null, chunk, (buff_index < len - 1) );		
				packet.setChecksum( calculatePacketChecksum( packet ) );
				packet_bytes = objectToBytes(packet); 
				m_socket.T_sendto(packet_bytes, packet_bytes.length, m_toAddr);
				
				// receive ack packet
				S_setSoTimeout(TIMEOUT);
				try {
					DatagramPacket ackPacket = m_socket.T_recvfrom(RECEIVE_PACKET_SIZE);
					System.out.println("CLIENT RECEIVED SOMETHING!");
				
					// deserialize
					ackPacketData = ackPacket.getData();
					S_StreamPacket streamPacket = (S_StreamPacket)bytesToObject(ackPacketData);
					if (streamPacket == null) continue;
					
					byte[] tempPacketChecksum = streamPacket.getChecksum();
					packetChecksum = new byte[tempPacketChecksum.length];
					for( int a = 0; a < tempPacketChecksum.length; a++ ) {
						packetChecksum[a] = tempPacketChecksum[a];
					}

					streamPacket.setChecksum(null);
					byte[] dataChecksum = calculatePacketChecksum(streamPacket);
					
					System.out.println("+++++++++++++++ Seq: " + m_seq + " NEW SEQ: " + streamPacket.getSequenceNumber() );
					System.out.println("+++++++++++++++ Ack: " + m_ack + " NEW ACK: " + streamPacket.getAcknowledgementNumber() );
				
					if (dataChecksum!= null) {
						System.out.print("dataChecksum is " );
						for(int a = 0; a < dataChecksum.length; a++) {
							System.out.print(dataChecksum[a]);
						}
						System.out.println();
					}
					
					if (packetChecksum!= null) {
						System.out.print("packetChecksum is " );
						for(int a = 0; a < packetChecksum.length; a++) {
							System.out.print(packetChecksum[a]);
						}
						System.out.println();
					}
				
					if ( MessageDigest.isEqual(dataChecksum, packetChecksum) && streamPacket.getSequenceNumber() == m_ack && streamPacket.getAcknowledgementNumber() != m_seq) {
						m_seq = streamPacket.getAcknowledgementNumber();
						m_ack = streamPacket.getSequenceNumber();
						break;
					}
				}
				catch (SocketTimeoutException e) {
					System.out.println("RECEIVING BACK ACK TIMEOUT");
					continue;
				}
			}
			
			if( i == MAX_SEND_ATTEMPTS ) { System.out.println("HAHAH EVEN PROGRAM GIVES UP"); }
			
			System.out.println("buff index: " + buff_index + ", len: " + len);

			if (buff_index == len) break;
		}
		System.out.println("done sent");
	}

	/**
	 * Receive an array of data.
	 *
	 * Args:
	 *   buf			The array of bytes to store the data.
	 *   len			The length of the array used to store the data.
	 *
	 * Returns:
	 *   Returns the actual number of bytes received.
	 */
    public int S_receive(byte[] buf, int len)
    {
		byte[] dataChecksum;
		byte[] packetChecksum;
		byte[] data = null;
		byte[] ackPacketBytes = null;
		int curIndex = 0;
		DatagramPacket packet;
		int prev_ack = 0;
		
		try {
			while (true) {	
				S_setSoTimeout(TIMEOUT);
				
				try {
					// receive packet
					packet = m_socket.T_recvfrom(RECEIVE_PACKET_SIZE);
				} catch (SocketTimeoutException e) {
					System.out.println("RECEIVING FROM SEND IS TIMING OUT");
					
					ackPacketBytes = objectToBytes(m_ackPacket);
					if (m_toAddr != null) {
						m_socket.T_sendto(ackPacketBytes, ackPacketBytes.length, m_toAddr);
					}
					continue;
				}
					
				// deserialize
				S_StreamPacket streamPacket = (S_StreamPacket)bytesToObject(packet.getData());
				// if deserialize failed, start again
				if (streamPacket == null) continue;
				
				// get data
				data = streamPacket.getData();
				
				// verify checksum
				packetChecksum = streamPacket.getChecksum();
				streamPacket.setChecksum(null);
				dataChecksum = calculatePacketChecksum(streamPacket);
				
					
				System.out.print("packetChecksum is " );
				for(int a = 0; a < packetChecksum.length; a++) {
					System.out.print(packetChecksum[a]);
				}
				System.out.println();
				
									
				System.out.print("dataChecksum is " );
				for(int a = 0; a < dataChecksum.length; a++) {
					System.out.print(dataChecksum[a]);
				}
				System.out.println();
		
				System.out.println("packetChecksum: " + packetChecksum.length);
				System.out.println("dataChecksum: " + dataChecksum.length);
				System.out.println("same: " +  MessageDigest.isEqual(packetChecksum, dataChecksum));
				System.out.println();

				
				if (data != null) {
					if (streamPacket.getState() == S_StreamSocket.STATE_SYN || (streamPacket.getAcknowledgementNumber() != m_ack)) {
						int minLen = Math.min(len-curIndex, data.length);
						if (minLen > 0) {
							System.arraycopy(data, 0, buf, curIndex, minLen);
							curIndex += minLen;
						}
					}

                    // handle close request
                    if (streamPacket.getState() == S_StreamSocket.STATE_CLD) {
                        this.S_close();
                        return 0;
                    }
				}
				
				if (streamPacket.getState() == S_StreamSocket.STATE_SYN && m_toAddr == null && !streamPacket.getMP()) {
					InetSocketAddress addr = (InetSocketAddress) bytesToObject(buf);
					System.out.println("accepted addr: " + addr);
					m_toAddr = addr;
				}
				
				System.out.println("+++++++++++++++ Seq: " + m_seq + " NEW SEQ: " + streamPacket.getSequenceNumber() );
				System.out.println("+++++++++++++++ Ack: " + m_ack + " NEW ACK: " + streamPacket.getAcknowledgementNumber() );
				System.out.println("+++++++++++++++ Prev Ack: " + prev_ack );
				prev_ack = m_ack;

				
				if (m_toAddr != null) {
					System.out.println("curIndex: " + curIndex + ", len " + len);
					// send the ack packet to the sender

					// If the checksum is not the same, we request a new packet.
					if (!MessageDigest.isEqual(packetChecksum, dataChecksum)) {
						System.err.println("CHECKSUM ERROR");	
						continue;
						//m_ackPacket = new S_StreamPacket(0, STATE_ERR, m_ack, m_seq, dataChecksum, null, false); 
					}
					else if (len == 0 || curIndex < len) {
						if (streamPacket.getAcknowledgementNumber() != m_ack) {
							m_ack = streamPacket.getAcknowledgementNumber();	
							m_seq = streamPacket.getSequenceNumber() + curIndex + 1;
						} 
						System.err.println("RECEIVE SENDING SHIT BACK TO SAY I GOT CRAP WITH SEQ: " + m_seq + " ACK: " + m_ack);
						m_ackPacket = new S_StreamPacket(0, m_state, m_ack, m_seq, null, null, false); 
						m_ackPacket.setChecksum( calculatePacketChecksum( m_ackPacket ) );
					} 
					else {
						System.err.println("ERROR IN S_RECEIVE");	
						// TODO: Why is this the same?! WTF!?
						m_ack = streamPacket.getAcknowledgementNumber();		
						m_seq = streamPacket.getSequenceNumber();
						m_ackPacket = new S_StreamPacket(0, m_state, m_ack, m_seq, null, null, false); 
						m_ackPacket.setChecksum( calculatePacketChecksum( m_ackPacket ) );
					}
					
					ackPacketBytes = objectToBytes(m_ackPacket);
					m_socket.T_sendto(ackPacketBytes, ackPacketBytes.length, m_toAddr);
				}

				// check if there is any more data and that the correct packet is received
				/*if( !streamPacket.getMP() ) {
					if (streamPacket != null && prev_ack != m_ack) {
						break;
					} else {
						curIndex = 0;
					}
				} */
				if(  streamPacket != null && !streamPacket.getMP() && prev_ack != m_ack) { break; }
			}
		} catch (Exception e)
		{
			System.out.println("RECIEVE ERROR! " + e.getMessage());
		}
		
		System.out.println("done receive");
		return curIndex;
    }

    /**
	 * Used to close the connection.
	 */
    public void S_close() /* throws ... */
    {
        // reset variables (similar to constructor)
		m_state = S_StreamSocket.STATE_CLD;
		m_seq = 0;
		m_ack = 0;

        S_StreamPacket packet = new S_StreamPacket(0, m_state, m_seq, m_ack, null, null, 0);
        byte[] packet_bytes = objectToBytes(packet); 
        m_socket.T_sendto(packet_bytes, packet_bytes.length, m_toAddr);
		m_socket.T_close();

        // don't want to store the other address anymore
        m_toAddr = null;
    }
	
	/**
	 * A helper method to serialize an object into an array of bytes.
	 *
	 * Args:
	 * 	 o					The object to serialize.
	 *
	 * Returns:
	 *   Returns an array of bytes that represents the object.
	 */
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

	/**
	 * A helper method to deserialize an array of bytes to its object representation.
	 *
	 * Args:
	 *   bytes				The array of serialized bytes to be deserialized.
	 *
	 * Returns:
	 *   Returns the object representation of the deserialized stream of bytes.
	 */
	private Object bytesToObject(byte[] bytes){
		Object result = null;
		try {

			ByteArrayInputStream bs = new ByteArrayInputStream(bytes);
			ObjectInputStream os = new ObjectInputStream(bs);
			result = os.readObject();
			os.close();
			bs.close();
		} catch (Exception e) {
			System.err.println("bytesToObject FAILING US! " + e.getMessage());
		}

		return result;
	}
	
	/**
	 * A helper method to calculate a checksum of some data.
	 *
	 * Args:
	 *   data				An array of data to calculate the checksum.
	 *
	 * Returns:
	 *   Returns a checksum of the data in an array of bytes.
	 */
	private byte[] calculateChecksum(byte[] data)
	{
		byte[] checksum;
		// Calculate the checksum
		try {
			MessageDigest md = MessageDigest.getInstance(DIGEST_ALGORITHM_NAME);
			checksum = md.digest(data);			
		} catch( NoSuchAlgorithmException e ) {
			checksum = new byte[1];
		}
		return checksum;
	}
}
