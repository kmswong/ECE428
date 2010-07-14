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
	private static final int NUM_WORD_CHECK = 5;
	private static final int NUM_CHAR_CHECK = 10;
	
	public static void main(String[] args) throws IOException {
	
		int f = Integer.parseInt(args[0]);
		int n = Integer.parseInt(args[1]);
		DatagramSocket serverSocket = null;
		
		HashMap<String, Integer> wordMap = getWordMap(FILENAME_WORDS);
		HashMap<String, Integer> charMap = getWordMap(FILENAME_CHARS);
		
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
					
					if( pCheck( deciphered ) ) {
				
						System.out.println(decipheredStr);
						System.out.println();
					}
				} while ( cg.hasMore() );
				
			}
		} catch (Exception e) {
			e.printStackTrace();
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
