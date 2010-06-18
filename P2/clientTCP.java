/**
* clientTCP.java
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
public class clientTCP 
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
		if (args.length != 2)
		{
            System.err.println("USAGE: java clientTCP <input filename> <country>");
            System.exit(ERROR_CODE);
		}

		// Store parameters from command line arguments.
		String filename = args[0];
		String country = args[1];
		
		// Variables used for TCP connection.
		S_StreamSocket socket = null;
		
		ArrayList players = null;
		
		// Busy loop to wait for the port filename to exist. If it does, retrieve the port number.
		int port = getPortNumber();
		System.out.println("port: " + port);
		
		// Get list of players and establish connection.
		try
		{
			players = getPlayers(filename);
			
            InetSocketAddress socketAddress = new InetSocketAddress(IP, port);
			socket = new S_StreamSocket(socketAddress);
		
			// Send country to server.
            sendString(socket, country);
			
			// Send how many players to send.
            int numPlayers = players.size();
            sendData(socket, numPlayers);
			
			// Send list of players to server.
			for (int i = 0; i < numPlayers; i++)
			{
                sendString(socket, players.get(i).toString());
			}
			
			FileWriter fstream = new FileWriter(OUTPUT_FILENAME);
			BufferedWriter toFile = new BufferedWriter(fstream);
			
			// Reading the number of players to receive from server.
            numPlayers = Integer.parseInt(receiveString(socket));

			// If there are no players returned for the country. Client expects
			// a message from the server (one line only).
			if(numPlayers == 0)
			{
                String msg = receiveString(socket);
				toFile.write(msg);
			}
			// Otherwise, read each player name from the server and write it out to outTCP.dat.
			else 
			{
                String player = receiveString(socket);
				toFile.write(player);
				// Read the players that are returned by the server.
				for(int i = 1; i < numPlayers; i++)
				{
                    player = receiveString(socket);
					toFile.write("\n" + player);
				}
			}
			
			// Close everything before program ends.
			socket.S_close();
			toFile.close();
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

        int receivedBytes = socket.S_receive(buf, len);

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
        socket.S_send(buf, buf.length);
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
    }

	/**
	 * Retrieve a list of players based on the given filename.
	 *
	 * Args:
	 *	 filename			The filename of the input file.
	 *
	 * Returns:				A list of players that are present in the input file.
	 *
	 * Throws:				IOException
	 */
	private static ArrayList getPlayers(String filename) throws IOException
	{
		ArrayList players = new ArrayList();
		
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = in.readLine()) != null)
		{
			players.add(line);
		}
		return players;
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
