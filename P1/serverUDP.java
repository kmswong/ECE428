import java.io.*;
import java.net.*;
import java.util.*;

public class serverUDP extends Thread {

    protected DatagramSocket socket = null;
    protected BufferedReader in = null;
    protected HashTable teams = null;
    protected ArrayList players = null;

    public UDPServerThread() throws IOException {
        this("UDPServerThread");
    }

    public UDPServerThread(String name) throws IOException {
        super(name);
        socket = new DatagramSocket(13524);
        teams = new HashTable();
        players = new ArrayList();

        try {
            in = new BufferedReader(new FileReader("in.dat"));
        } catch (FileNotFoundException e) {
            System.err.println("Could not open quote file. Serving time instead.");
        }
    }

    public void run() {

        for (;;) {
            try {
                byte[] buf = new byte[256];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // figure out response
                buf = packet.getData()

                // send the response to the client at "address" and "port"
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        packet = new DatagramPacket(buf, buf.length, address, port);
        socket.send(packet);
        socket.close();
    }

    public static void main(String[] args) throws IOException {
      new serverUDP().start();
    }
}
