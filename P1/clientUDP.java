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
        FileReader inputStream = null;
        FileWriter outputStream = null;

        // get a datagram socket
        DatagramSocket socket = new DatagramSocket();

        // send request
        byte[] buf;
        InetAddress address = InetAddress.getByName("127.0.0.1");

        try {
            inputStream = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = inputSteam.readLine()) != null) {
                buf = line.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 13524);
                socket.send(packet);
            }
            buf = country.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 13524);
            socket.send(packet);
            buf = new String("DONE").getButes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 13524);
            socket.send(packet);
        }
        finally {
            if (in != null) {
                in.close();
            }
        }

        // get response
        try {
            outputStream = new PrintWriter(new FileWriter("out.dat"));
            for (;;) {
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                if (received.equals("DONE")) {
                    break;
                }
                outputStream.println(received)
            }
        }
        finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }

        socket.close();
    }
}

 
