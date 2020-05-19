import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class CoordinatorClient implements Runnable {
    private int coordinatorPort;
    private int timeout;
    public int listeningPort;
    public boolean foundHost = false;
    private Thread thread;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private ParticipantWrapper participantWrapper;

    public CoordinatorClient(HashMap<String, String> properties, ParticipantWrapper participantWrapper) {
        this.participantWrapper = participantWrapper;
        this.timeout = Integer.parseInt(properties.get(Constants.TIMEOUT));
        this.coordinatorPort = Integer.parseInt(properties.get(Constants.COORDINATOR_PORT));
        this.listeningPort = participantWrapper.listeningPort;
        connectToCoordinator();
    }

    public void connectToCoordinator() {
        while (!this.foundHost) {
            try {
                socket = new Socket("127.0.0.1", coordinatorPort);
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.flush();
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                foundHost = true;
                objectOutputStream.writeObject(Constants.MSG_JOIN + " " + this.listeningPort);
                System.out.println("Sent message to coordinator : " + Constants.MSG_JOIN + " " + this.listeningPort);
                objectOutputStream.flush();
                System.out.println("CONNECTED TO Coordinator");
            } catch (Exception e) {
                System.out.println("Couldnt connect to server...");
                foundHost = false;
            }
        }
        thread = new Thread(this, "CoordinatorClient for " + listeningPort);
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
                e.printStackTrace();
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
            case Constants.MSG_VOTE_OPTIONS:
                System.out.println("Received voting options, preparing voting infrastructure");
                //remove the header
                parsedMessage.remove(0);
                participantWrapper.votingOptions.addAll(parsedMessage);
                participantWrapper.initiateInfrastructure();
                break;
            case Constants.MSG_DETAILS:
                parsedMessage.remove(0);
                System.out.println("Received " + Constants.MSG_DETAILS + " with " +parsedMessage);
                participantWrapper.participantPorts.addAll(parsedMessage);
                break;
            default:
                System.out.println(parsedMessage);
                System.out.println("MESAJ PE CLIENT ????????????" + parsedMessage);
        }
    }
}
