/**
* p3server.java
* Author: Kevin Xiao Long Lu
* Course: ECE428
* Written in 2010
*/

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.*;

public class p3server 
{
	private static final int ERROR_CODE = -1;
	private static final String TXT_KEY_CHANGE = "KEY CHANGE";
	private static final String FILENAME_WORDS = "hamlet_word_out.txt";
	private static final String FILENAME_CHARS = "hamlet_char_out.txt";
	private static final String FILENAME_OUTDAT = "out.dat";
	private static final int NUM_WORD_CHECK = 5;
	private static final int NUM_CHAR_CHECK = 10;
	
	public static void main(String[] args) throws IOException {
		int f = Integer.parseInt(args[0]);
		int n = Integer.parseInt(args[1]);
		DatagramSocket serverSocket = null;
		
		HashMap<String, Integer> wordMap = getWordMap(FILENAME_WORDS);
		HashMap<String, Integer> charMap = getWordMap(FILENAME_CHARS);
		// HACK: add empty space.
		charMap.put(" ", 0);
		
		try 
		{
		    serverSocket = new DatagramSocket();
		    serverSocket.setReuseAddress(true);
		}
		catch (IOException e)
		{
		    System.err.println("[ERROR] Could not listen on port: " + e.getMessage());
		    System.exit(ERROR_CODE);
		}
		
		// write the port number to the environemnt variable P
		System.out.println("Server's port: " + serverSocket.getLocalPort());

		try {
			Integer port = new Integer(serverSocket.getLocalPort());
			ProcessBuilder pb = new ProcessBuilder( "/home/tripunit/p3client", "-s", port.toString(), "-f", args[0], "-n", args[1]);
			pb.start();
		} catch (Exception e) {
			System.err.println( e.getMessage() );
		}
		
		// Open File for out.dat
		File file = new File(FILENAME_OUTDAT);
		FileWriter fstream = new FileWriter(FILENAME_OUTDAT);
		BufferedWriter toOutputFile = new BufferedWriter(fstream);

		System.out.println("Receiving~~");

		try {
			DatagramPacket packet = null;
			Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
			CombinationGenerator cg = new CombinationGenerator(128, n);
			
			byte[] buf = null; 

			byte[] iv = new byte[16];
			for( int i = 0; i < 16; i++ ) {
				iv[i] = 0;
			}
			
			IvParameterSpec ivs = new IvParameterSpec( iv );
			
			for (;;) {		
		
				buf = new byte[1024];

				// receive request
				packet = new DatagramPacket(buf, buf.length);
				serverSocket.receive(packet);

				byte[] receivedByte = packet.getData();
				byte[] byteToDecrypt = new byte [packet.getLength()];
				
				System.arraycopy(receivedByte , 0, byteToDecrypt , 0, packet.getLength());
				String receivedData = new String(byteToDecrypt);
				
				System.out.println( "+++++++++++++++++++++++++++++++++++++++NEW PACKET+++++++++++++++++++++++++");
				System.out.println( receivedData );
				System.out.println( packet.getLength() + " " + receivedByte.length );
				
				
				// TODO: Change from continue to SOMETHING ELSE.
				if ((receivedData.trim()).equals(TXT_KEY_CHANGE)) {
					continue;
				}
				
				System.out.println();	
				cg.reset();
				do {
					byte[] key = nextKey(cg);

					c.init( Cipher.DECRYPT_MODE, new SecretKeySpec( key, "AES" ), ivs );
					byte[] deciphered = c.doFinal( byteToDecrypt  );
					String decipheredStr = new String( deciphered );
					
					//System.out.println();
					//System.out.println("Deciphered Text *************************************");					
					//System.out.println(decipheredStr);
					
					boolean check = pCheck( deciphered );
					System.out.println("pCheck: " + check);
					
					// Do char and word checking.

					if( check ) {
						boolean wordCheck = true;
						boolean charCheck = true;
						int decipheredStrSize = decipheredStr.length();
						for (int i  = 0; i < NUM_CHAR_CHECK && i < decipheredStrSize; ++i) {
							String decipheredChar = String.valueOf(decipheredStr.charAt(i));
							if (!charMap.containsKey(decipheredChar)) {
								//TODO: invalid char!!!!!
								charCheck = false;
								//System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@ invalid char: " + decipheredChar + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
							}
						}
						
						String[] decipheredWords = decipheredStr.split(" ");
						int decipheredWordsSize = decipheredWords.length;
						for (int i  = 1; i < NUM_WORD_CHECK + 1 && i < decipheredWordsSize; ++i) {
							String decipheredWord = decipheredWords[i].replace('\r',' ').trim().toLowerCase();
							if (!wordMap.containsKey(decipheredWord)) {
								//TODO: invalid word!!!!!
								wordCheck = false;
								//System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@ invalid word: " + decipheredWord + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
							}
						}
						
						if (wordCheck)
						{
							// We have deciphered the text! Now to output the text.
							//toOutputFile.write(decipheredStr);
							//toOutputFile.write("I HAVE DECIPHERED THE TEXT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
							//toOutputFile.newLine();
							System.out.println(decipheredStr);
							System.out.println("I HAVE DECIPHERED THE TEXT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
							//toOutputFile.flush();
							break;
						}
					}
				} while ( cg.hasMore() );
				// At this point, we failed to find the key
				//toOutputFile.write("I'M A NOOB!");
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} finally 
		{
			toOutputFile.close();
		}
	}
	
	private static byte[] nextKey(CombinationGenerator cg)
	{
		int[] nextKeyInt = cg.getNext();

		byte[] retKey = new byte[ 16 ];
		
		for( int i = 0; i < 16; i++ ) {
			retKey[i] = -1;
		}

		for (int i = 0; i < nextKeyInt.length; i++)
		{
			int pos = nextKeyInt[i];
			retKey[ pos / 8 ] = (byte)(retKey[ pos / 8 ] & (byte)( 255 - Math.pow( 2, 8 - pos % 8 - 1)));
		}

		return retKey;
	}

	private static boolean pCheck(byte[] data)
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
	
	private static HashMap<String, Integer> getWordMap(String filename)
	{
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		
		try
		{
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				map.put(strLine.trim(), 0);
			}
			
			//Close the input stream
			in.close();
		}
		catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
		}
		
		return map;
	  
	}
}
