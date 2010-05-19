import java.io.*;
import java.net.*;
import java.util.*;


class clientUDP {
    // Constants.
	private static final String IP = "127.0.0.1";
	private static final String PORT_FILENAME = "portUDP.ini";

    // Variables
    private String filename;
    private String country;
    private DatagramSocket socket = null;
    private int port;
    private InetAddress address  = null;

    public clientUDP(String filename, String country, DatagramSocket socket) {
        this.filename = filename;
        this.country = country;
        this.socket = socket;
        this.address = this.socket.getInetAddress();
    }

    public void sendPlayers() {
        // read the input file
        FileReader inputStream = new BufferedReader(new FileReader(filename));

        try {
            // send the country data to the server first
            this.sendPacket(this.country);

            // loop through the lines in the file and send each one to the
            // server after it has been read
            String line;
            while ((line = inputStream.readLine()) != null) {
                this.sendPacket(line);
            }

            // tell the server that we are done sending data
            this.sendPacket("DONE");
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private void sendPacket(String data) {
        byte[] buf;
        buf = data.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length,
                                                   this.address, this.port);
        this.socket.send(packet);
    }

    public void receivePlayers() {
        FileWriter outputStream = new PrintWriter(new FileWriter("out.dat"));

        try {
            String received = this.receivePacket();
            int numPlayers = Integer.parseInt(received);

            if (numPlayers == 0) {
                outputStream.println(this.country + " did not qualify for the world cup.");
            }
            else {
                for (int i = 0; i < Integer.parseInt(numPlayers); i++) {
                    String player = this.receivePacket();
                    outputStream.println(player)
                }
            }
        }
        finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private String receivePacket() {
        byte[] buf new byte[1024];
        packet = new DatagramPacket(buf, buf.length);
        this.socket.receive(packet);
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
        int port = this.getPortNumber();

        // create a datagram socket
        DatagramSocket socket = new DatagramSocket(IP, port);

        clientUDP client = new clientUDP(args[0], args[1], socket);
        // send country and player data to server
        client.sendPlayers();

        // receive team player list from server
        client.receivePlayers();

        // close the socket
        socket.close();
    }
}

 
