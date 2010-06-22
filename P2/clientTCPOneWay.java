/**
* clientTCPOneWay.java
* Author: Aaron Chun Win Yuen
* Course: ECE428
* Written in 2010
*/

import java.io.*;
import java.net.*;
import java.util.*;

/**
* The client class that uses the TCP protocol to implement the World Cup players program. The program
* makes use of a file that reads what port the serverTCP is listening to. At the end of the execution,
* the client deletes that port file.
*/
public class clientTCPOneWay 
{
	// Constants.
	private static final String IP = "127.0.0.1";
	private static final int ERROR_CODE = 1;
	private static final String OUTPUT_FILENAME = "outTCP.dat";
	private static final String PORT_FILENAME = "portTCP.ini";
	
	/**
	 * Main method.
	 */
	public static void main(String [] args)
	{
		// If the number of arguments do not match what is expected. Show how the 
		// program should be used and exit the program.
		if (args.length != 1)
		{
            System.err.println("USAGE: java clientTCPOneWay <input filename>");
            System.exit(ERROR_CODE);
		}

		// Store parameters from command line arguments.
		String filename = args[0];
		
		// Variables used for TCP connection.
		S_StreamSocket socket = null;
        InetSocketAddress loopback = new InetSocketAddress("127.0.0.1", 0);
		
		ArrayList lines = null;
		
		// Busy loop to wait for the port filename to exist. If it does, retrieve the port number.
		int port = getPortNumber();
		System.out.println("port: " + port);
		
		// Get list of players and establish connection.
		try
		{
			lines = getPlayers(filename);
			
            InetSocketAddress serverSocketAddress = new InetSocketAddress(IP, port);
			
			socket = new S_StreamSocket(loopback);
			
			System.out.println("CONNECT TO SERVER");
			socket.S_connect(serverSocketAddress);
			
			// Send how many lines to send.
            int numLines = lines.size();
            sendData(socket, numLines);
			
            long now = System.currentTimeMillis();
			// Send list of lines to server.
			for (int i = 0; i < numLines; i++)
			{
                sendString(socket, lines.get(i).toString());
			}
            System.err.println("sending data from filename " + filename + " took: " + (System.currentTimeMillis() - now) + "ms");
			
			// Close everything before program ends.
			socket.S_close();
		}
		catch (UnknownHostException e)
		{
			System.err.println("Host cannot be found: " + e.getMessage());
			System.exit(ERROR_CODE);
		}
		catch (IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(ERROR_CODE);
		}
	}

    private static String receiveString(S_StreamSocket socket)
    {
		int len = 1024;
		byte[] buf = new byte[len];
		int receivedBytes = 0;

		receivedBytes = socket.S_receive(buf, len);
		return new String(buf, 0, receivedBytes);
    }

    private static void sendData(S_StreamSocket socket, int data)
    {
        sendString(socket, Integer.toString(data));
    }

    private static void sendString(S_StreamSocket socket, String data)
    {
        byte[] buf = data.getBytes();
		try {
		System.out.println("sending: " + data);
        socket.S_send(buf, buf.length);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
    }

	/**
	 * Retrieve a list of lines based on the given filename.
	 *
	 * Args:
	 *	 filename			The filename of the input file.
	 *
	 * Returns:				A list of lines that are present in the input file.
	 *
	 * Throws:				IOException
	 */
	private static ArrayList getPlayers(String filename) throws IOException
	{
		ArrayList lines = new ArrayList();
		
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = in.readLine()) != null)
		{
			lines.add(line);
		}
		return lines;
	}
	
	/**
	 * Busy loop to wait for the port filename to exist. If it does, the method retrieves the port number.
	 */
	private static int getPortNumber()
	{
		int port = -1;
		try
		{
			File f = null;
			while(true)
			{
				f = new File(PORT_FILENAME);
				if(f.exists())
				{
					String line = new BufferedReader(new FileReader(PORT_FILENAME)).readLine();
					port = Integer.parseInt(line);
					break;
				}
			}
		}
		catch (IOException e)
		{
			System.err.println("[ERROR] Error getting port number.");
		}
		return port;
	}
}
