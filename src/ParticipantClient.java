import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ParticipantClient implements Runnable{
    private int participantServerPort;
    private int timeout;
    private Thread thread;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    boolean foundHost = true;
    private ObjectInputStream objectInputStream;
    private ParticipantWrapper participantWrapper;

    public ParticipantClient(String participantServerPort, ParticipantWrapper participantWrapper) {
        this.participantWrapper = participantWrapper;
        this.participantServerPort = Integer.parseInt(participantServerPort);
        this.timeout = participantWrapper.timeout;
        connectToCoordinator();
    }

    public void connectToCoordinator() {
        try {
            socket = new Socket("127.0.0.1", participantServerPort);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            System.out.println("Couldnt connect to server...");

        }
        thread = new Thread(this, "ParticipantClient " + participantServerPort);
        thread.start();
    }

    public synchronized void run(){
        receiveObject();
    }

    public void receiveObject() {
        while (foundHost) {
            try {
                Object object = objectInputStream.readObject();
                handle((String) object);
            } catch (Exception e){
                foundHost=false;
//                e.printStackTrace();
            }
        }
    }

    public void sendObject(Object object){
        try {
            this.objectOutputStream.writeObject(object);
            this.objectOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handle(String message){
        ArrayList<String> parsedMessage = new ArrayList<>(Arrays.asList(message.split(" ")));
        String header = parsedMessage.get(0);
        switch (header){
            default:
                System.out.println("DEFAULT REACHED ON PARTICIPANT CLIENT WITH THE MESSAGE : " + parsedMessage);
        }
    }
}
