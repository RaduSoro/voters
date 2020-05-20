import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CoordinatorServer implements Runnable{

    private int port;
    private int loggerPort;
    private int maxParticipants;
    private int timeout;
    public boolean outcome = false;
    private ServerSocket serverSocket;
    private HashMap<String, String> properties;
    private ArrayList<SocketWrapper> clientList;
    private ArrayList<String> votingOptions;
    private Socket socket;
    private Thread thread;

    public CoordinatorServer(HashMap<String, String> properties, ArrayList<String> votingOptions) {
        this.properties = properties;
        this.votingOptions = votingOptions;
        clientList = new ArrayList<>();

        this.port = Integer.parseInt(properties.get(Constants.PORT));
        this.loggerPort = Integer.parseInt(properties.get(Constants.LOGGER_PORT));
        this.maxParticipants = Integer.parseInt(properties.get(Constants.PARTICIPANTS));
        this.timeout = Integer.parseInt(properties.get(Constants.TIMEOUT));

        thread = new Thread(this, "Coordinator");
        thread.start();
    }

    public synchronized void run() {
        System.out.println("Running coordinator server, listening on port: " + this.properties.get(Constants.PORT));
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (maxParticipants!=0 && !outcome) try {
            socket = serverSocket.accept();
            thread = new Thread(){
                @Override
                public void run() {
                    SocketWrapper socketWrapper = new SocketWrapper(socket);
                    clientList.add(socketWrapper);
                    //infinte read object function
                    readObject(socketWrapper);
                    //if it ever exits close the client
                    try {
                        clientList.remove(socketWrapper);
                        socketWrapper.socket.close();
                        this.interrupt();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
            maxParticipants--;
            Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close(){
        clientList.forEach(socketWrapper -> {
            try {
                socketWrapper.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setupVote(){
        if (maxParticipants == 0){
            System.out.println("Setup vote");
            String voteOpt = Constants.MSG_VOTE_OPTIONS;
            for (String option: votingOptions) {
                voteOpt += " " + option;
            }
            //send message to all the clients with the listening port of the other clients
            for (SocketWrapper socketWrapper : clientList){
                String options = Constants.MSG_DETAILS;
                for (SocketWrapper socketWrapper_1 : clientList){
                    if (!socketWrapper.listeningPort.equals(socketWrapper_1.listeningPort)){
                        options += " "+socketWrapper_1.listeningPort;
                    }
                }
                sendMessage(socketWrapper,options);
            }
            broadcast(voteOpt);
        }
    }

    private void readObject(SocketWrapper socketWrapper) {
        while (!socketWrapper.socket.isClosed() && !outcome){
            try {
                Object receivedObject = socketWrapper.objectInputStream.readObject();
                handleMessage((String) receivedObject,socketWrapper);
            } catch (Exception e) {
                if (!outcome){
                    System.out.println(socketWrapper.listeningPort + "has crashed");
                }
            }
        }
    }

    public void sendMessage(SocketWrapper socketWrapper, String message){
        if (!socketWrapper.socket.isClosed()){
            try {
                socketWrapper.objectOutputStream.writeObject(message);
                socketWrapper.objectOutputStream.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast (String message){
        clientList.forEach(client -> sendMessage(client,message));
    }

    private void handleMessage(String message, SocketWrapper socketWrapper) {
        ArrayList<String> parsedMessage = new ArrayList<>(Arrays.asList(message.split(" ")));
        String header = parsedMessage.get(0);
        //remove the header so we can work with the proper message
        parsedMessage.remove(0);
        switch (header){
            case Constants.MSG_JOIN:
                socketWrapper.listeningPort = parsedMessage.get(0);
                setupVote();
                break;
            case Constants.MSG_VOTE:
                System.out.println("VOTE REACHED WITH" + parsedMessage);
                break;
            case Constants.MSG_OUTCOME:
                outcome = true;
                try {
                    serverSocket.close();
                    thread.interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(message);
                break;
            default:
                System.out.println("DEFAULT REACHED WITH  " + parsedMessage);
                break;
        }

    }
}
