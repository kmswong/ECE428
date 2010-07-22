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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharacterCodingException;


public class p3server 
{
	private static final int ERROR_CODE = -1;
	private static final String TXT_KEY_CHANGE = "KEY CHANGE";
	private static final String FILENAME_WORDS = "hamlet_word_out.txt";
	private static final String FILENAME_OUTDAT = "out.dat";
	private static final boolean DEBUG = true;
	
	public static void main(String[] args) throws IOException {
		int f = Integer.parseInt(args[0]);
		int n = Integer.parseInt(args[1]);
		DatagramSocket serverSocket = null;
		
		HashMap<String, Integer> wordMap = getWordMap(FILENAME_WORDS);
		
		
		try 
		{
			// Set up the server socket and port
		    serverSocket = new DatagramSocket();
		    serverSocket.setReuseAddress(true);
		}
		catch (IOException e)
		{
		    System.err.println("[ERROR] Could not listen on port: " + e.getMessage());
		    System.exit(ERROR_CODE);
		}
		
		try {
			// Fork a process that runs the client program with the server's port as an input parameter
			Integer port = new Integer(serverSocket.getLocalPort());
			ProcessBuilder pb = new ProcessBuilder( "/home/tripunit/p3client", "-s", port.toString(), "-f", args[0], "-n", args[1]);
			pb.start();
		} catch (Exception e) {
			System.err.println( e.getMessage() );
		}
		
		// Open File for out.da
		FileWriter fstream = new FileWriter(FILENAME_OUTDAT);
		BufferedWriter toOutputFile = new BufferedWriter(fstream);
		
		byte[] correctKey = null;

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
				
				if (DEBUG) {
					System.out.println( "+++++++++++++++++++++++++++++++++++++++NEW PACKET+++++++++++++++++++++++++");
					//System.out.println( receivedData );
					System.out.println( packet.getLength() + " " + receivedByte.length );
				}
				
				
				// TODO: Change from continue to SOMETHING ELSE.
				if ((receivedData.trim()).equals(TXT_KEY_CHANGE)) {
					correctKey = null;
					continue;
				}
				
				if (correctKey == null) {
					cg.reset();

					//spawn off one sequential worker thread and one random worker thread to get the key					
					Worker seqWorker = new Worker(0, 128, n, cg, byteToDecrypt, wordMap);
					Thread seqThread = new Thread(seqWorker);
					
					//Worker randWorker = new RandomKeyWorker(1, 128, n, cg, byteToDecrypt, wordMap);
					//Thread randThread = new Thread(randWorker);
					
					// all the workers have to initialize before any of them are start
					seqThread.start();
					//randThread.start();
					
					//randThread.join();
					seqThread.join();
					correctKey = seqWorker.getKey();
					//if (correctKey == null) {
					//	correctKey = randWorker.getKey();
					//}					
				}
				
				
				if (correctKey != null) {
					c.init( Cipher.DECRYPT_MODE, new SecretKeySpec( correctKey, "AES" ), ivs );
					byte[] deciphered = c.doFinal( byteToDecrypt  );
					String decipheredStr = new String( deciphered );
											
					// We have deciphered the text! Now to output the text.
					toOutputFile.write(decipheredStr);
					//toOutputFile.write("I HAVE DECIPHERED THE TEXT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					toOutputFile.newLine();
					toOutputFile.flush();
					
					if (DEBUG) {
						System.out.println(decipheredStr);
						System.out.println("I HAVE DECIPHERED THE TEXT!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
					}
				} else {
					// At this point, we failed to find the key
					//toOutputFile.write("I'M A NOOB!");
				}
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


