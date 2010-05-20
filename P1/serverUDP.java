import java.io.*;
import java.net.*;
import java.util.*;

public class serverUDP extends Thread {
    // Constants.
    private static final int ARGS_LENGTH = 0;
    private static final int ERROR_CODE = 1;
    private static final String PORT_FILENAME = "portUDP.ini";
    private static final String TERMINATION = "DONE";

    public static void main(String[] args) throws IOException {
        System.out.println("RUNNING serverUDP");

        
        // If the number of arguments do not match what is expected. Show how the 
        // program should be used and exit the program.
        if (args.length != ARGS_LENGTH)
        {
            showUsage();
        }

        DatagramSocket serverSocket = null;
        try 
        {
            serverSocket = new DatagramSocket();
            serverSocket.setReuseAddress(true);
            //serverSocket.bind(null);
        }
        catch (IOException e)
        {
            System.err.println("[ERROR] Could not listen on port: " + e.getMessage());
            System.exit(ERROR_CODE);
        }
        
        // Write the port to the port filename so that the client knows which port to use to
        // establish the connection.
        System.out.println(serverSocket.getLocalPort());
        writePortNumber(serverSocket.getLocalPort());

        ArrayList playersInCountry = new ArrayList();
        String country = "";
        DatagramPacket packet = null;
        byte[] buf = null; 
        for (;;) {
            try {
                buf = new byte[1024];

                // receive request
                packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);

                String receivedData = new String(packet.getData());
                if (receivedData.trim().equalsIgnoreCase(TERMINATION)) {
                    break;
                }

                String[] playerAndCountry = receivedData.trim().split(" ");
                if (playerAndCountry.length > 1 && playerAndCountry[1].equalsIgnoreCase(country)) {
                    playersInCountry.add(playerAndCountry[0]);
                } else if (playerAndCountry.length == 1) {
                    country = playerAndCountry[0];
                }   
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (packet != null) {
            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            // send number of players
            String numPlayers = Integer.toString(playersInCountry.size());
            System.out.println("Number of players in country: " + playersInCountry.size());
            buf = numPlayers.getBytes();
            packet = new DatagramPacket(buf, buf.length, address, port);
            serverSocket.send(packet);

            for (int i = 0; i < playersInCountry.size(); i++) {
                String sendData = (String)playersInCountry.get(i);
                buf = sendData.getBytes();
                packet = new DatagramPacket(buf, buf.length, address, port);
                serverSocket.send(packet);
            }
        }
        serverSocket.close();
    }

    
    /**
     * Display a usage message to inform the user how to use this program in the command line.
     * This method also exits the program as well.
     */
    private static void showUsage()
    {
        System.err.println("USAGE: java serverUDP");
        System.exit(ERROR_CODE);
    }


    /**
     * Write the port to the port filename so that the client knows which port to use to
     * establish the connection.
     *
     * Args:
     *   port                The port to write to port file. 
     */
    private static void writePortNumber(int port)
    {
        try
        {
            File file = new File(PORT_FILENAME);
            if(file.exists())
            {
                file.delete();
                //System.out.println("file deleted first");
            }
            
            FileWriter fstream = new FileWriter(PORT_FILENAME);
            BufferedWriter toPortFile = new BufferedWriter(fstream);
            
            toPortFile.write(String.valueOf(port));
            toPortFile.close();
            //System.out.println("Port written to file: " + PORT_FILENAME);
        }
        catch (IOException e)
        {
            System.err.println("[ERROR] Cannot write port to port file");
        }
    }
}
