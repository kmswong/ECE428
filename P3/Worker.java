import java.io.*;
import java.math.BigInteger;
import java.net.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharacterCodingException;

class Worker implements Runnable
{
	protected static final int NUM_WORD_CHECK = 5;
	protected static final int NUM_CHAR_CHECK = 10;
	protected static final boolean DEBUG = true;
	
	protected int m_n;
	protected int m_r;
	protected CombinationGenerator m_cg;
	protected byte[] m_data;
	protected HashMap<String, Integer> m_wordMap;
	protected byte[] m_key;
	protected IvParameterSpec m_ivs;
	protected boolean m_done;
	protected static boolean m_keyFound = false;
	
	protected int m_id;
	
	private static final HashSet<Character> validChars = new HashSet<Character>(Arrays.asList(
		new Character[] {
			'0','1','2','3','4','5','6','7','8','9', ' ','.',',','-','!','\'','?',';',':','[',']','\n',
			'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
			'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'			
		}));

	public Worker(int id, int n, int r, CombinationGenerator cg, byte[] data, HashMap<String, Integer> wordMap) {
		this.m_cg = cg;
		this.m_data = data;
		this.m_wordMap = wordMap;
		this.m_key = null;
		this.m_done = false;
		this.m_n = n;
		this.m_r = r;
		
		this.m_id = id;
		
		m_keyFound = false;
	}
	
	public void run() {
		try {
			Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
			
			byte[] iv = new byte[16];
			for( int i = 0; i < 16; i++ ) {
				iv[i] = 0;
			}
			
			m_ivs = new IvParameterSpec( iv );
			
			this.nextKey();
				
			int tmpCnt = 1;
			int tmpCnt2 = 1;
			long time = System.currentTimeMillis();
			
			while (!this.m_done && !m_keyFound) {			

						if (tmpCnt % 200001 == 0) {
							System.err.println("worker " + this.m_id + " has gone through " + tmpCnt2 + "*200,000 keys in " + (System.currentTimeMillis() - time) + "ms");
							tmpCnt = 0;
							tmpCnt2++;
						}
						tmpCnt++;
						
				// Decrypt the receive packet with the next generated key
				byte[] key = nextKey();
				if (key == null) {
					break;
				}
				
				c.init( Cipher.DECRYPT_MODE, new SecretKeySpec( key, "AES" ), m_ivs );
				byte[] deciphered = c.doFinal( m_data  );
				String decipheredStr = new String( deciphered );
				
				// get the actual data string without the parity byte
				String textStr = decipheredStr.substring(1, decipheredStr.length());	
												
				boolean wordCheck = true;
				
				//TODO: find a new way to parse the string into tokens, the tokenizer seems to be pretty slow
				StringTokenizer tokens = new StringTokenizer(textStr, "\n\r ");
				int decipheredWordsSize = tokens.countTokens();
				
				if (decipheredWordsSize <= 0) {
					System.err.println("word size is <= 0!!!!!!!!!!!!!");
					continue;
				} else if (decipheredWordsSize >= NUM_WORD_CHECK + 2) {
					//do word checks; do not check the first and the last words since they might get truncated
					tokens.nextToken();
					
					for (int i  = 0; i < NUM_WORD_CHECK && i < decipheredWordsSize - 2; ++i) {
						String decipheredWord = tokens.nextToken().trim().toLowerCase();
						
						if (!m_wordMap.containsKey(decipheredWord)) {
							//TODO: invalid word!!!!!
							wordCheck = false;
							//System.err.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@ invalid word: " + decipheredWord + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
							break;
						}
					}
				}
				
				if( wordCheck && isPureAscii(textStr) && pCheck( deciphered )) {			
					this.m_key = key;
					synchronized(this) {
						m_keyFound = true;
					}
					break;
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		this.m_done = true;
		
		if (this.m_key == null)
			System.err.println("worker " + m_id + " is done with key == null ");
		else 
			System.err.println("worker " + m_id + " is done with key != null ");
	}
	
	public byte[] getKey() {
		return this.m_key;
	}
	
	public void done() {
		this.m_done = true;
	}
	
	public byte[] nextKey()
	{
		int[] nextKeyInt = this.m_cg.getNext();
		if (nextKeyInt == null) {
			return null;
		}
		
		byte[] retKey = new byte[ 16 ];
		
		for( int i = 0; i < 16; i++ ) {
			retKey[i] = -1;
		}
		for (int i = 0; i < nextKeyInt.length; i++)
		{
			int pos = nextKeyInt[i];
			retKey[ pos / 8 ] = (byte)(retKey[ pos / 8 ] ^ (byte)(1 << (pos % 8)));
		}
		
		return retKey;
	}
	
	public boolean isPureAscii(String str) {
			for(int i = 0 ; i < str.length() && i < NUM_CHAR_CHECK; i += 3) {
				if (validChars.contains(str.charAt(i)) == false) {
					//System.err.println("@@@@@@@@@@@@@@@ invalid char: " + str.charAt(i));
					return false;
				}
			}

			byte bytearray []  = str.getBytes();
			CharsetDecoder d = Charset.forName("US-ASCII").newDecoder();
			try {
				CharBuffer r = d.decode(ByteBuffer.wrap(bytearray));
				r.toString();
			}
			catch(CharacterCodingException e) {
				return false;
			}
			return true;
		
		
	}
  
	private boolean pCheck(byte[] data)
	{
		int dataLength = data.length;
		if (dataLength < 2) 
		{
			return false;
		}
		
		Byte origPByte = new Byte(data[0]);
		byte newPByte = data[1];
		for (int i = 2; i < dataLength; i++) 
		{
			newPByte = (byte)(newPByte ^ data[i]);
		}
		
		if (origPByte.equals(newPByte)) {
			return true;
		}
		
		return false;
	}
}

class RandomKeyWorker extends Worker
{
	private static final int POS_OVERHEAD = 1;
	private static final int NUM_TRIALS = 30;
	private HashSet<String> testedKeys = new HashSet<String>();
	
	public RandomKeyWorker(int id, int n, int r, CombinationGenerator cg, byte[] data, HashMap<String, Integer> wordMap) {
		super(id, n, r, cg, data, wordMap);
	}
	
	public byte[] nextKey()
	{
		if (this.m_r > 0) {
			// get the lowest bit of the last sequential key that is changed based on the combination generator
			int cgLowestPos = this.m_cg.getLowestPos();
			if (cgLowestPos < (this.m_n - POS_OVERHEAD - this.m_r)) {
				Random random = new Random();
				while (true) {
					byte[] retKey = new byte[ 16 ];
					
					for(int i = 0; i < 16; i++) {
						retKey[i] = -1;
					}
					
					LinkedList<Integer> positions = new LinkedList<Integer>();
					
					// every bit is at least POS_OVERHEAD higher than the lowest bit of the sequential key
					int startPos = cgLowestPos + POS_OVERHEAD;
					for (int i = 0; i < this.m_r; ) {
						int pos = random.nextInt(this.m_n - startPos) + startPos;
						if (!positions.contains(pos) ) {
							positions.add(pos);
							retKey[ pos / 8 ] = (byte)(retKey[ pos / 8 ] ^ (byte)(1 << (pos % 8)));
							i++;
						}
					}
					String keyStr = "";
					for(int i = 0; i < 16; i++) {
						keyStr += (int)retKey[i];
					}
					
					if (testedKeys.contains(keyStr)) {
						continue;
					}
					testedKeys.add(keyStr);
					
					return retKey;
				}
			} else {
				System.err.println("boooo highest pos: " + cgLowestPos + "; n: " + this.m_n);
			}
		}
		return null;
	}
}
