import java.io.*;
import java.net.*;
import java.util.*;

public class clientTCP {
	private static final String IP = "127.0.0.1";
	private static final int ARGS_LENGTH = 2;
	private static final int PORT = 23984;
	private static final int ERROR_CODE = 1;
	private static final String OUTPUT_FILENAME = "outTCP.dat";
	
	public static void main(String [] args)
	{
		System.out.println("RUNNING clientTCP");

		// If the number of arguments do not match what is expected. Show the 
		// program should be used.
		if (args.length != ARGS_LENGTH)
		{
			ShowUsage();
		}

		// Store parameters from command line arguments.
		String filename = args[0];
		String country = args[1];
		
		// Variables used for TCP connection.
		Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
		
		ArrayList players = null;
		
		// Get list of players and establish connection.
		try
		{
			players = GetPlayers(filename);
			
			socket = new Socket(IP, PORT);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		
			// Send country to server.
			out.println(country);
			
			// Send how many players to send.
			out.println(players.size());
			
			// Send list of players to server.
			for (int i = 0; i < players.size(); i++)
			{
				
				out.println(players.get(i).toString());
			}
			
			// Reading the number of players to receive from server.
			int numPlayers = Integer.parseInt(in.readLine());

			// Reader the player.
			for(int i = 0; i < numPlayers; i++)
			{
				String player = in.readLine();
				System.out.println(i + ": " + player);
			}
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
		
		System.out.println("STOPPING clientTCP");		
	}

	private static void ShowUsage()
	{
		System.err.println("USAGE: java clientTCP <input filename> <country>");
		System.exit(ERROR_CODE);
	}

	private static ArrayList GetPlayers(String filename) throws IOException
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
}
