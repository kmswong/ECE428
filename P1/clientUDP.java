import java.io.*;
import java.net.*;

class clientUDP {
    // Constants.
	private static final String IP = "127.0.0.1";
	private static final String PORT_FILENAME = "portUDP.ini";
    private static final String TERMINATION_STRING = "DONE";

    // Variables
    private String filename;
    private String country;
    private DatagramSocket socket = null;
    private int port;
    private InetAddress address  = null;

    public clientUDP(String filename, String country, DatagramSocket socket, int port) {
        this.filename = filename;
        this.country = country;
        this.socket = socket;
        try {
            this.address = InetAddress.getByName(IP);
        }
        catch (Exception e) {
        }
        this.port = port;
    }

    public void sendPlayers() {
        // read the input file
        BufferedReader inputStream = null;

        try {
            inputStream = new BufferedReader(new FileReader(filename));
            // send the country data to the server first
            this.sendPacket(this.country);

            // loop through the lines in the file and send each one to the
            // server after it has been read
            String line;
            while ((line = inputStream.readLine()) != null) {
                this.sendPacket(line);
            }

            // tell the server that we are done sending data
            this.sendPacket(TERMINATION_STRING);

            inputStream.close();
        }
        catch (Exception e) {
            System.out.println("[ERROR] Error sending players to server.");
        }
    }

    private void sendPacket(String data) {
        byte[] buf;
        buf = data.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length,
                                                   this.address, this.port);
        try {
            this.socket.send(packet);
        }
        catch (Exception e) {
            System.out.println("[ERROR] Error sending packet to server: " + e.getMessage());
        }
    }

    public void receivePlayers() {
        PrintWriter outputStream = null;

        try {
            outputStream = new PrintWriter(new FileWriter("out.dat"));
            String received = this.receivePacket();
            int numPlayers = Integer.parseInt(received);

            if (numPlayers == 0) {
                outputStream.println(this.country + " did not qualify for the world cup.");
            }
            else {
                for (int i = 0; i < numPlayers; i++) {
                    String player = this.receivePacket();
                    outputStream.println(player);
                }
            }

            outputStream.close();
        }
        catch (Exception e) {
            System.out.println("[ERROR] Error receiving players from server.");
        }
    }

    private String receivePacket() {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            this.socket.receive(packet);
        }
        catch (Exception e) {
            System.out.println("[ERROR] Error receiving packet from server.");
        }
        return new String(packet.getData(), 0, packet.getLength());
    }

    private static int getPortNumber() {
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

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
             System.out.println("Usage: java clientUDP [filename] [country]");
             return;
        }
        
        // pick up port from server-generated file
        System.out.println("Getting port number");
        int port = getPortNumber();
        System.out.println("Port number is: " + port);

        // create a datagram socket
        DatagramSocket socket = new DatagramSocket();

        clientUDP client = new clientUDP(args[0], args[1], socket, port);
        // send country and player data to server
        System.out.println("Sending players");
        client.sendPlayers();

        // receive team player list from server
        System.out.println("Receiving players");
        client.receivePlayers();

        System.out.println("DONE. Closing socket");
        // close the socket
        socket.close();
    }
}

 
