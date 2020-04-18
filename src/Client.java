import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Client implements Runnable  {

    private Socket socket;
    private int cordinatorPort;
    private int loggerPort;
    private int timeout;
    private Thread thread;
    private int port;
    private VoterWrapper voterWrapper;
    private boolean foundHost = false;
    private String type;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;


    private ArrayList<String> arguments;

    public Client(ArrayList<String> arguments, VoterWrapper voterWrapper, String type){
        this.type = type;
        this.arguments = arguments;
        this.voterWrapper = voterWrapper;
        if (type.equals(MSG.COORDINATOR))this.initParticipant();
        else this.initVoter();
    }

    private void initVoter() {
        System.out.println("INIT VOTER");
        System.out.println(this.arguments);
        this.loggerPort = Integer.parseInt(this.arguments.get(1));
        this.port = Integer.parseInt(this.arguments.get(7));
        this.timeout = Integer.parseInt(this.arguments.get(3));
        connectAsClient(this.port);
    }


    private void initParticipant(){
        this.cordinatorPort = Integer.parseInt(this.arguments.get(0));
        this.loggerPort = Integer.parseInt(this.arguments.get(1));
        this.port = Integer.parseInt(this.arguments.get(2));
        this.timeout = Integer.parseInt(this.arguments.get(3));
//        thread = new Thread(this, "Participant " + this.port);
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
                if (this.type.equals(MSG.COORDINATOR)){
                    objectOutputStream.writeObject(MSG.JOIN + " " + this.port);
                    objectOutputStream.flush();
                }
                System.out.println("CONNECTED TO SERVER");
            } catch (Exception e) {
                System.out.println("Couldnt connect to server...");
                foundHost = false;
            }
        }
        thread = new Thread(this, "client "+ port);
        thread.start();
    }

    public synchronized void run(){
        receiveObject();
    }

    public void receiveObject() {
        while (foundHost) {
            try {
                Object o = objectInputStream.readObject();
                handle((String) o);
            } catch (Exception e){
                foundHost=false;
                e.printStackTrace();
            }
        }
    }

    public void handle(String tokens){
        ArrayList<String> parsedTokens =   new ArrayList<>(Arrays.asList(tokens.split(" ")));
        String header = parsedTokens.get(0);
        switch (header){
            case MSG.VOTE_OPTIONS:
                System.out.println("VOTING OPTIONS REACHED");
                voterWrapper.initiateInfrastucture(parsedTokens);
                break;
            case MSG.DETAILS:
                parsedTokens.remove(0);
                voterWrapper.getDetails().addAll(parsedTokens);
            break;

            default:
                System.out.println(parsedTokens);
        }
    }

}



