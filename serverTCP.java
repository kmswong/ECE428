import java.net.*;
import java.io.*;

public class serverTCP {
	public static void main( String args[] ) throws IOException {
		System.out.println("host name: " + InetAddress.getLocalHost());
		ServerSocket serverSocket = null;

		try {
            serverSocket = new ServerSocket();
	    serverSocket.bind( new InetSocketAddress("127.0.0.1", 23984));
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + e.getMessage());
            System.exit(1);
        }

	System.out.println(serverSocket.getLocalSocketAddress()); 
        Socket clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }
	System.out.println("Connected");
        
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

	try
	{  
		String country = in.readLine();
		System.out.println( country );
		int num_of_players = Integer.parseInt( in.readLine() );
		System.out.println( num_of_players );
		
		for( int x = 0; x < num_of_players; x++ ) {
			String player = in.readLine();
System.out.println( player );
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
}
