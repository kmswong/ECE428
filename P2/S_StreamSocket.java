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
	private int m_sendSeq;
	private int m_sendAck;
	private int m_receiveSeq;
	private int m_receiveAck;
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
	
	private int m_id;
	
	private static final String DIGEST_ALGORITHM_NAME = "MD5";
	
	private final int TIMEOUT = 500;

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
		m_sendAck = 0;
		m_sendSeq = 0;
		m_receiveAck = 0;
		m_receiveSeq = 0;
		m_id = 1;
		m_ackPacket = new S_StreamPacket(0, STATE_ERR, m_sendSeq, m_sendAck, null, null, false);
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
		byte[] dataChecksum = null;
		byte[] tempPacketChecksum = null;
		
		int buff_index = 0;
		int chunkSize = Math.min(CHUNK_SIZE, len);
		byte [] chunk;
		
		// Stream Packet Info
		S_StreamPacket packet;
		byte[] checksum;
		byte[] packet_bytes;			// serialized version of packet
		m_id++;							// Increase the ID of the packet being sent
		
		while (true) {

			// Increase the Acknowledgement number
			m_sendAck += 1;
			
			// make a chunk
			chunk = new byte [chunkSize];
			// copy data from buff to chunk
			for (int i = 0; i < chunkSize && buff_index < len; i++)
			{
				chunk[i] = buf[buff_index++];
			}

			// Try to send the packet
			for (int i = 0; i < MAX_SEND_ATTEMPTS; i++)
			{
				// send packet using underlying UDP interface			
				packet = new S_StreamPacket(m_id, m_state, m_sendSeq, m_sendAck, null, chunk, (buff_index < len - 1) );		
				packet.setChecksum( calculatePacketChecksum( packet ) );
				packet_bytes = objectToBytes(packet); 
				m_socket.T_sendto(packet_bytes, packet_bytes.length, m_toAddr);
				
				// receive ack packet
				S_setSoTimeout(TIMEOUT);
				try {
					DatagramPacket ackPacket = m_socket.T_recvfrom(RECEIVE_PACKET_SIZE);
				
					// Deserialize the packet
					ackPacketData = ackPacket.getData();
					S_StreamPacket streamPacket = (S_StreamPacket)bytesToObject(ackPacketData);
					if (streamPacket == null) continue;
					
					// Calculate Checksum
					tempPacketChecksum = streamPacket.getChecksum();
					packetChecksum = new byte[tempPacketChecksum.length];
					for( int a = 0; a < tempPacketChecksum.length; a++ ) {
						packetChecksum[a] = tempPacketChecksum[a];
					}
					streamPacket.setChecksum(null);
					dataChecksum = calculatePacketChecksum(streamPacket);
				
					// Only stop sending the packet if the checksum of the ACK package matches
					if ( MessageDigest.isEqual(dataChecksum, packetChecksum) && streamPacket.getSequenceNumber() == m_sendAck && streamPacket.getAcknowledgementNumber() != m_sendSeq && streamPacket.getId() == m_id) {
						m_sendSeq = streamPacket.getAcknowledgementNumber();
						m_sendAck = streamPacket.getSequenceNumber();
						break;
					}
				}
				catch (SocketTimeoutException e) {
					continue;
				}
			}
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
		int prevAck = 0;
		DatagramPacket packet;

		while (true) {	
			try {
				S_setSoTimeout(TIMEOUT);
		
				// Try to receive a packet, if it times out, resend the previous ACK packet
				try {
					packet = m_socket.T_recvfrom(RECEIVE_PACKET_SIZE);
				} catch (SocketTimeoutException e) {
					ackPacketBytes = objectToBytes(m_ackPacket);
					if (m_toAddr != null) {
						m_socket.T_sendto(ackPacketBytes, ackPacketBytes.length, m_toAddr);
					}
					continue;
				}
					
				// Deserialize the packet received
				S_StreamPacket streamPacket = (S_StreamPacket)bytesToObject(packet.getData());
				
				// if deserialize failed, start again
				if (streamPacket == null) continue;

				// Verify checksum
				data = streamPacket.getData();				
				packetChecksum = streamPacket.getChecksum();
				streamPacket.setChecksum(null);
				dataChecksum = calculatePacketChecksum(streamPacket);

				// Put the data into the buffer if the checksum is the same
				if (MessageDigest.isEqual(packetChecksum, dataChecksum)) {
					if (data != null) {
						if (streamPacket.getState() == S_StreamSocket.STATE_SYN || (streamPacket.getAcknowledgementNumber() != m_receiveAck)) {
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

					// If it is a SYN request, retrieve the to address
					if(streamPacket.getState() == S_StreamSocket.STATE_SYN && m_toAddr == null && !streamPacket.getMP()) {
						InetSocketAddress addr = (InetSocketAddress) bytesToObject(buf);
						m_toAddr = addr;
					}
				} else {
					curIndex = 0;
				}
				
				// Update the acknowledgement number
				prevAck = m_receiveAck;
				
				if (m_toAddr != null) {
					// If the checksum does not match, reset the index and do not send the ACK packet back
					if (!MessageDigest.isEqual(packetChecksum, dataChecksum)) {	
						curIndex = 0;
						continue;						
					} else if (len == 0 || curIndex < len) {
						// Send the ACK request back to acknowledge packet is received.
						if (streamPacket.getAcknowledgementNumber() != m_receiveAck) {
							m_receiveAck = streamPacket.getAcknowledgementNumber();	
							m_receiveSeq = streamPacket.getSequenceNumber() + curIndex + 1;
						} 
						m_ackPacket = new S_StreamPacket(streamPacket.getId(), m_state, m_receiveAck, m_receiveSeq, null, null, false); 
						m_ackPacket.setChecksum( calculatePacketChecksum( m_ackPacket ) );
						
						ackPacketBytes = objectToBytes(m_ackPacket);
						m_socket.T_sendto(ackPacketBytes, ackPacketBytes.length, m_toAddr);
					} else {		
						continue;
					}
				}

				// Exit if there is no more packets to receive
				if( streamPacket != null && !streamPacket.getMP() && prevAck != m_receiveAck) { break; }
			} catch (Exception e) {
			}
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
		m_sendSeq = 0;
		m_sendAck = 0;

        S_StreamPacket packet = new S_StreamPacket(0, m_state, m_sendSeq, m_sendAck, null, null, false);
        byte[] packet_bytes = objectToBytes(packet); 
		try {
			m_socket.T_sendto(packet_bytes, packet_bytes.length, m_toAddr);
		} catch (Exception e ){
			System.err.println( e.getMessage() );
			
		} finally {
			m_socket.T_close();
		}

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
