/**
 * S_StreamPacket.java
 * Authors: John Huang, Kevin Lu, Ka Man Sophia Wong, Aaron Chun Win Yuen
 * Course: ECE428
 * Written in 2010
 */

import java.io.*;

/**
 * A Stream Packet class used as a data structure to send between the server and client.
 */
public class S_StreamPacket implements Serializable
{
	// Data members.
	private int m_sequenceNumber;
	private int m_acknowledgementNumber;
	private byte[] m_checksum;
	private int m_currentState;
	private boolean m_mp;
	private int m_id;
		
	private byte[] m_data;
	
	/**
	 * Constructor.
	 *
	 * Args:
	 *   id						The id of the packet.
	 *   s						The current state of the packet.
	 *   sequenceNumber			The sequence number of the packet.
	 *   ackNumber				The acknowledgement number of the packet.
	 *   checksum				The array of checksum bytes for the packet.
	 *   data					The array of bytes of the data.
	 *   mp						A flag indicating more packets needed to assemble (similar to more fragments).
	 */
	public S_StreamPacket( int id, int s, int sequenceNumber, int ackNumber, byte[] checksum, byte[] data, boolean mp )
	{
		this.m_id = id;
		this.m_currentState = s;
		this.m_sequenceNumber = sequenceNumber;
		this.m_acknowledgementNumber = ackNumber;
		this.m_checksum = checksum;
		this.m_data = data;
		this.m_mp = mp;
	}
	
	/**
	 * Retreives the ID of the packet.
	 *
	 * Returns:
	 *   Returns the ID of the packet.
	 */
	public int getId() {
		return this.m_id;
	}
	
	/**
	 * Retrieves the state of the packet.
	 *
	 * Returns:
	 *   Returns the state of the packet.
	 */
	public int getState(){
		return this.m_currentState;
	}
	
	/**
	 * Retrieves the sequence number of the packet.
	 *
	 * Returns:
	 *   Returns the sequence number of the packet.
	 */
	public int getSequenceNumber()
	{
		return this.m_sequenceNumber;
	}
	
	/**
	 * Retrieves the acknowledgement number of the packet.
	 *
	 * Returns:
	 *   Returns the acknowledgement number of the packet.
	 */
	public int getAcknowledgementNumber()
	{
		return this.m_acknowledgementNumber;
	}
	
	/**
	 * Retrieves the checksum of the packet.
	 *
	 * Returns:
	 *   Returns the checksum of the packet.
	 */
	public byte[] getChecksum()
	{
		return this.m_checksum;
	}
	
	/**
	 * Retrieves the data of the packet.
	 *
	 * Returns:
	 *   Returns the data of the packet.
	 */
	public byte[] getData()
	{
		return this.m_data;
	}
	
	/**
	 * Retrieves the more packets flag of the packet.
	 *
	 * Returns:
	 *   Returns the more packets flag of the packet.
	 */
	public boolean getMP()
	{
		return this.m_mp;
	}
	
	/**
	 * A overriden toString method to return a string representation of the packet.
	 */
	public String toString()
	{
		return "Packet:\t" + this.getId() +
			"\nState:\t" + this.getState() +
			"\nSequenceNumber:\t" + this.getSequenceNumber() + 
			"\nAcknowledgementNumber:\t" + this.getAcknowledgementNumber() + 
			"\nMF:\t" + this.getMP() ;
	}
	
	public void setChecksum( byte[] checksum )
	{
		this.m_checksum = checksum;
	}
}
