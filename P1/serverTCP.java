import java.net.*;
import java.io.*;
import java.util.*;

public class serverTCP {
	public static void main( String args[] ) throws IOException {
		ServerSocket serverSocket = null;

		try {
            serverSocket = new ServerSocket();
	    serverSocket.bind( new InetSocketAddress("127.0.0.1", 23984));
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + e.getMessage());
            System.exit(1);
        }

        Socket clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

	try
	{  
		String country = in.readLine();
		int numOfLines = Integer.parseInt( in.readLine() );

		// Check if country is found by checking if there are number of plaeyrs
		ArrayList listOfPlayers = new ArrayList();
		
		for( int x = 0; x < numOfLines; x++ ) {
			listOfPlayers.add(in.readLine());
		}

		//process the players
		ArrayList processedPlayers = getPlayersInCountry( country, listOfPlayers );
		out.println( processedPlayers.size() );

		if( processedPlayers.size() > 0 ) { 
			for( int x = 0; x < processedPlayers.size(); x++ ) {
				out.println( processedPlayers.get(x) );
			}
		} else { 
			out.println( country + " did not qualify to the world cup" );
		}

        }
        catch(NumberFormatException nfe)
        {
        	System.out.println("Numbers not intergers");
        	out.println("Numbers not intergers");
        }
        
        
        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();
    }

    private static ArrayList getPlayersInCountry( String country, ArrayList listOfPlayers )
    {
	ArrayList listOfPlayersInCountry = new ArrayList();
	// Iterate through each player and store the ones in the specified country 
	for( int x = 0; x < listOfPlayers.size(); x++ ) {
		String[] playerAndCountry = ((String)listOfPlayers.get(x)).split(" ");
		if( playerAndCountry[1].equals( country ) ) {
			listOfPlayersInCountry.add( playerAndCountry[0] );
		}
	}

	return listOfPlayersInCountry;
    }	
}
