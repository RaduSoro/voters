import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class UDPLoggerServer {
    private static int port;
    private static DatagramSocket socket;
    private static PrintStream ps;
    static boolean socketNotTimedOut = true;
    public static void main(String[] args) throws IOException {
        ps = new PrintStream("logger_server_" + System.currentTimeMillis() + ".log");
        port = Integer.parseInt(args[0]);
        {
            try {
                socket = new DatagramSocket(port);
//                System.out.println(port);
            } catch (SocketException e) {
                e.printStackTrace();
                socketNotTimedOut = false;
            }
        }
       Thread thread = new Thread(){
           @Override
           public void run() {
               try {
                   read();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       };
       thread.start();

    }
    public static void read() throws IOException, SocketException {
        byte[] receiveByte = new byte[65535];
        DatagramPacket receivedPacket = null;
        while (socketNotTimedOut){
            // Step 2 : create a DatgramPacket to receive the data.
            receivedPacket = new DatagramPacket(receiveByte, receiveByte.length);
            // Step 3 : revieve the data in byte buffer.
            socket.receive(receivedPacket);
//            socket.setSoTimeout(10000);
            handle(data(receiveByte).toString(),receivedPacket);
            // Exit the server if the client sends "bye"
            if (data(receiveByte).toString().equals("bye"))
            {
                System.out.println("Client sent bye.....EXITING");
                break;
            }
            // Clear the buffer after every message.
            receiveByte = new byte[65535];
        }
    }
    public static void handle(String data, DatagramPacket receivedPacket){
        ArrayList<String> tokens =  new ArrayList<>(Arrays.asList(data.split(" ")));
        String header = tokens.get(0);
        String stringWithoutHeader = "";
        for (String token : tokens) {
            stringWithoutHeader += token + " ";
        }
        ps.println(header + " " +  System.currentTimeMillis() + stringWithoutHeader);
        try{
            acknowledge(receivedPacket);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static void acknowledge(DatagramPacket received) throws IOException {
        String ack = "ack";
        InetAddress ip = received.getAddress();
        int clientPort = received.getPort();
        byte[] bytes = ack.getBytes();
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, ip, clientPort);
        socket.send(packet);
    }
    //random stuff from stack overflow to convert byte[] to string
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
}
