/**
* p3server.java
* Author: Kevin Xiao Long Lu
* Course: ECE428
* Written in 2010
*/

import java.io.*;
import java.net.*;
import java.util.*;

public class p3server 
{
	private static final int ERROR_CODE = -1;

	public static void main(String[] args) throws IOException {
		DatagramSocket serverSocket = null;
		try 
		{
		    serverSocket = new DatagramSocket();
		    serverSocket.setReuseAddress(true);
		    //serverSocket.bind(null);
		}
		catch (IOException e)
		{
		    System.err.println("[ERROR] Could not listen on port: " + e.getMessage());
		    System.exit(ERROR_CODE);
		}
		
		// write the port number to the environemnt variable P
		//String command = "set P=" + serverSocket.getLocalPort();
		String command = "rm -f temp";
		System.out.println(serverSocket.getLocalPort());

		try {
			Integer port = new Integer(serverSocket.getLocalPort());
			ProcessBuilder pb = new ProcessBuilder( "/home/tripunit/p3client", "-s", port.toString(), "-f", "0", "-n", "0");
			Process p = pb.start();

			/*BufferedReader br = new BufferedReader( new InputStreamReader(p.getInputStream()));
			System.out.println("began reader..");
			String line = null;
			while((line = br.readLine()) != null )
				System.out.println(line);

				System.out.println( "done");*/
		} catch (Exception e) {
			System.err.println( e.getMessage() );
		}

		System.out.println("Receiving~~");
		
		String country = "";
		DatagramPacket packet = null;
		byte[] buf = null; 
		for (;;) {
			try {
				buf = new byte[1024];

				// receive request
				packet = new DatagramPacket(buf, buf.length);
				serverSocket.receive(packet);

				String receivedData = new String(packet.getData());
				System.out.println( receivedData);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
