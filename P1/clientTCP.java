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
	private static final int ARGS_LENGTH = 2;
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
		if (args.length != ARGS_LENGTH)
		{
			showUsage();
		}

		// Store parameters from command line arguments.
		String filename = args[0];
		String country = args[1];
		
		// Variables used for TCP connection.
		Socket socket = null;
        PrintWriter toServer = null;
        BufferedReader fromServer = null;
		
		ArrayList players = null;
		
		// Busy loop to wait for the port filename to exist. If it does, retrieve the port number.
		int port = getPortNumber();
		
		// Get list of players and establish connection.
		try
		{
			players = getPlayers(filename);
			
			socket = new Socket(IP, port);
			toServer = new PrintWriter(socket.getOutputStream(), true);
			fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
			// Send country to server.
			toServer.println(country);
			
			// Send how many players to send.
			toServer.println(players.size());
			
			// Send list of players to server.
			for (int i = 0; i < players.size(); i++)
			{
				
				toServer.println(players.get(i).toString());
			}
			
			FileWriter fstream = new FileWriter(OUTPUT_FILENAME);
			BufferedWriter toFile = new BufferedWriter(fstream);
			
			// Reading the number of players to receive from server.
			int numPlayers = Integer.parseInt(fromServer.readLine());

			// If there are no players returned for the country. Client expects
			// a message from the server (one line only).
			if(numPlayers == 0)
			{
				String msg = fromServer.readLine();
				toFile.write(msg);
			}
			// Otherwise, read each player name from the server and write it out to outTCP.dat.
			else 
			{
				String player = fromServer.readLine();
				toFile.write(player);
				// Read the players that are returned by the server.
				for(int i = 1; i < numPlayers; i++)
				{
					player = fromServer.readLine();
					toFile.write("\n" + player);
				}
			}
			
			// Close everything before program ends.
			socket.close();
			toServer.close();
			fromServer.close();
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

	/**
	 * Display a usage message to inform the user how to use this program in the command line.
	 * This method also exits the program as well.
	 */
	private static void showUsage()
	{
		System.err.println("USAGE: java clientTCP <input filename> <country>");
		System.exit(ERROR_CODE);
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
