import java.io.Serializable;

public class S_StreamPacket implements Serializable{
	public enum State { SYN, ACK };
	private int m_sequenceNumber;
	private int m_acknowledgementNumber;
	private int m_checksum;
	private State m_currentState;
		
	private byte[] m_data;
	
	public S_StreamPacket( State s, int sequenceNumber, int ackNumber, int checksum, byte[] data )
	{
		this.m_currentState = s;
		this.m_sequenceNumber = sequenceNumber;
		this.m_acknowledgementNumber = ackNumber;
		this.m_checksum = checksum;
		this.m_data = data;
	}
	
	public State getState(){
		return this.m_currentState;
	}
	
	public int getSequenceNumber()
	{
		return this.m_sequenceNumber;
	}
	
	public int getAcknowledgementNumber()
	{
		return this.m_acknowledgementNumber;
	}
	
	public int getChecksum()
	{
		return this.m_checksum;
	}
	
	public byte[] getData()
	{
		return this.m_data;
	}
}
