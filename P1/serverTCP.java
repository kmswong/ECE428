/**
* serverTCP.java
* Author: Ka Man Sophia Wong
* Course: ECE428
* Written in 2010
*/

import java.io.*;
import java.net.*;
import java.util.*;

/**
* The server class that retrieves a country and a list of players from clientTCP and returns back to the client
* only the players that are playing for that country using the TCP protocol. To establish a connection, the
* server randomly selects a port, and writes this port number to a file, expecting the client to read the port
* number off this file.
*/
public class serverTCP 
{
	// Constants.
	private static final String IP = "127.0.0.1";
	private static final int ARGS_LENGTH = 0;
	private static final int ERROR_CODE = 1;
	private static final String PORT_FILENAME = "portTCP.ini";

	/**
	 * Main method.
	 */
	public static void main( String args[] ) throws IOException {
		System.out.println("RUNNING clientTCP");

		// If the number of arguments do not match what is expected. Show how the 
		// program should be used and exit the program.
		if (args.length != ARGS_LENGTH)
		{
			showUsage();
		}
		
		ServerSocket serverSocket = null;
		try 
		{
            serverSocket = new ServerSocket();
			serverSocket.bind(null);
        }
		catch (IOException e)
		{
            System.err.println("[ERROR] Could not listen on port: " + e.getMessage());
            System.exit(ERROR_CODE);
        }
		
		// Write the port to the port filename so that the client knows which port to use to
		// establish the connection.
		writePortNumber(serverSocket.getLocalPort());
		

        Socket clientSocket = null;
        try 
		{
            clientSocket = serverSocket.accept();
        }
		catch (IOException e)
		{
            System.err.println("[ERROR] Accept failed.");
            System.exit(ERROR_CODE);
        }
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

		try
		{  
			String country = in.readLine();
			int numOfLines = Integer.parseInt( in.readLine() );

			// Check if country is found by checking if there are number of plaeyrs.
			ArrayList listOfPlayers = new ArrayList();
			
			for( int x = 0; x < numOfLines; x++ )
			{
				listOfPlayers.add(in.readLine());
			}

			// Process the players.
			ArrayList processedPlayers = getPlayersInCountry( country, listOfPlayers );
			out.println( processedPlayers.size() );

			if( processedPlayers.size() > 0 )
			{ 
				for( int x = 0; x < processedPlayers.size(); x++ )
				{
					out.println( processedPlayers.get(x) );
				}
			} 
			else
			{ 
				out.println( country + " did not qualify to the world cup" );
			}

        }
        catch(NumberFormatException nfe)
        {
        	System.err.println("[ERROR] Numbers not intergers.");
        }
		
		// Close everything before program ends.
        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();
		
		// Delete the port file
		File file = new File(PORT_FILENAME);
		if (file.exists())
		{
			file.delete();
		}
    }
	
	/**
	 * Display a usage message to inform the user how to use this program in the command line.
	 * This method also exits the program as well.
	 */
	private static void showUsage()
	{
		System.err.println("USAGE: java serverTCP");
		System.exit(ERROR_CODE);
	}

	/**
	 * Given a list of players and a country, this method returns a list of only the players from that country.
	 * 
	 * Args:
	 *   country					The country to filter the list of players.
	 *   listOfPlayers				The list of players used to filter.
	 *
	 * Returns:						A list of players from the given list that are from the specified country.
	 */
    private static ArrayList getPlayersInCountry( String country, ArrayList listOfPlayers )
    {
		ArrayList listOfPlayersInCountry = new ArrayList();
		
		// Iterate through each player and store the ones in the specified country 
		for( int x = 0; x < listOfPlayers.size(); x++ )
		{
			String[] playerAndCountry = ((String)listOfPlayers.get(x)).split(" ");
			if( playerAndCountry[1].equals( country ) )
			{
				listOfPlayersInCountry.add( playerAndCountry[0] );
			}
		}

		return listOfPlayersInCountry;
    }

	/**
	 * Write the port to the port filename so that the client knows which port to use to
     * establish the connection.
	 *
	 * Args:
	 *   port				The port to write to port file. 
	 */
	private static void writePortNumber(int port)
	{
		try
		{
			File file = new File(PORT_FILENAME);
			if(file.exists())
			{
				file.delete();
				System.out.println("file deleted first");
			}
			
			FileWriter fstream = new FileWriter(PORT_FILENAME);
			BufferedWriter toPortFile = new BufferedWriter(fstream);
			
			toPortFile.write(String.valueOf(port));
			toPortFile.close();
		}
		catch (IOException e)
		{
			System.err.println("[ERROR] Cannot write port to port file");
		}
	}
}
