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
	private static final String PORT_FILENAME = "portTCP.ini";
	private static final int ERROR_CODE = 1;

	/**
	 * Main method.
	 */
	public static void main( String args[] ) throws IOException {
        if (args.length != 0) {
             System.out.println("USAGE: java serverTCP");
             return;
        }
		
		S_StreamSocket socket = null;
		try 
		{
            socket = new S_StreamSocket(null);
			//socket.bind(null);
        }
		catch (IOException e)
		{
            System.err.println("[ERROR] Could not listen on port: " + e.getMessage());
            System.exit(ERROR_CODE);
        }
		
		// Write the port to the port filename so that the client knows which port to use to
		// establish the connection.
        InetSocketAddress localAddress = socket.S_getLocalSocketAddress();
		writePortNumber(localAddress.getPort());

        InetSocketAddress clientSocket = null;
        try 
		{
            clientSocket = socket.S_accept();
        }
		catch (IOException e)
		{
            System.err.println("[ERROR] Accept failed.");
            System.exit(ERROR_CODE);
        }
        
		try
		{  
            // get country
            String country = receiveString(socket);
            int numLines = Integer.parseInt(receiveString(socket));

			// Check if country is found by checking if there are number of plaeyrs.
			ArrayList listOfPlayers = new ArrayList();
			
			for( int x = 0; x < numLines; x++ )
			{
				listOfPlayers.add(receiveString(socket));
			}

			// Process the players.
			ArrayList processedPlayers = getPlayersInCountry( country, listOfPlayers );
            int numPlayers = processedPlayers.size();
            sendData(socket, numPlayers);

			if( numPlayers > 0 )
			{ 
				for( int i = 0; i < numPlayers; i++ )
				{
                    sendString(socket, (String)(processedPlayers.get(i)));
				}
			} 
			else
			{ 
                sendString(socket, country + " did not qualify for the world cup");
			}

        }
        catch(NumberFormatException nfe)
        {
        	System.err.println("[ERROR] Numbers not integers.");
        }
		
		// Close everything before program ends.
        socket.S_close();
        //clientSocket.close();
		
		// Delete the port file
		File file = new File(PORT_FILENAME);
		if (file.exists())
		{
			file.delete();
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
		}catch(Exception e) {
			System.err.println(e.getMessage());
		}
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
