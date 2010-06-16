public class S_StreamPacket {
	public static final int STATE_DFT = -1;
	public static final int STATE_SYN = 0;
	public static final int STATE_ACK = 1;
	private int m_sequenceNumber;
	private int m_acknowledgementNumber;
	private int m_checksum;
	private int m_currentState;
	private boolean m_mp;
	private int m_id;
		
	private byte[] m_data;
	
	public S_StreamPacket( int id, int s, int sequenceNumber, int ackNumber, int checksum, byte[] data, boolean mp )
	{
		this.m_id = id;
		this.m_currentState = s;
		this.m_sequenceNumber = sequenceNumber;
		this.m_acknowledgementNumber = ackNumber;
		this.m_checksum = checksum;
		this.m_data = data;
		this.m_mp = mp;
	}
	
	public int getId() {
		return this.m_id;
	}
	
	public int getState(){
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
	
	public boolean getMP()
	{
		return this.m_mp;
	}
	
	public String toString()
	{
		return "Packet:\t" + this.getId() +
			"\nState:\t" + this.getState() +
			"\nSequenceNumber:\t" + this.getSequenceNumber() + 
			"\nAcknowledgementNumber:\t" + this.getAcknowledgementNumber() + 
			"\nChecksum:\t" + this.getChecksum() +
			"\nMF:\t" + this.getMP() ;
	}
}
