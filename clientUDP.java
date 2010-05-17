import java.io.*;
import java.net.*;
import java.util.*;


class clientUDP {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
             System.out.println("Usage: java clientUDP [filename] [country]");
             return;
        }
        String filename = args[0];
        String country = args[1];

        ArrayList players = new ArrayList();

        Scanner s = null;
        try {
            s = new Scanner(new BufferedReader(new FileReader(filename)));
            while (s.hasNext()) {
                players.add((Object)(s.next()));
            }
        }
        finally {
              if (s != null) {
                  s.close();
              }
        }

        // get a datagram socket
        DatagramSocket socket = new DatagramSocket();

        // send request
        byte[] buf = new byte[256];
        InetAddress address = InetAddress.getByName("127.0.0.1");

        for (int i = 0; i < players.size(); i++) {

            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 13524);
            socket.send(packet);
        }

        // get response
        packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);

        // display response
        String received = new String(packet.getData(), 0, packet.getLength());
        System.out.println("Quote of the Moment: " + received);

        socket.close();
    }
}

 
