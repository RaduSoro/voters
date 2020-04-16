import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Client {

    private Socket socket;
    private int cordinatorPort;
    private int loggerPort;
    private int timeout;
    private int port;
    private boolean foundHost = false;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;


    private ArrayList<String> arguments;

    public Client( String[] arguments){
        this.arguments = new ArrayList<>(Arrays.asList(arguments));
        this.initParticipant();
    }



    private void initParticipant(){
        this.cordinatorPort = Integer.parseInt(this.arguments.get(0));
        this.loggerPort = Integer.parseInt(this.arguments.get(1));
        this.port = Integer.parseInt(this.arguments.get(2));
        this.timeout = Integer.parseInt(this.arguments.get(3));
//        thread = new Thread(this, "Participant " + this.port);
        //create server
        //TODO CLIENT SERVER
        //create client
        connectAsClient(this.cordinatorPort);
    }

    private void connectAsClient(Integer port){
        while (!this.foundHost) {
            try {
                socket = new Socket("127.0.0.1", port);
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.flush();
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                foundHost = true;
                objectOutputStream.writeObject("HELLO WORLD");
                objectOutputStream.flush();
                System.out.println("CONNECTED TO SERVER");
            } catch (Exception e) {
                System.out.println("Couldnt connect to server...");
                foundHost = false;
            }
        }
        receiveObject();
    }

    public void receiveObject() {
        while (foundHost) {
            try {
                Object o = objectInputStream.readObject();
                System.out.println(o);
            } catch (Exception e){
                foundHost=false;
                e.printStackTrace();
            }
        }
    }

}

