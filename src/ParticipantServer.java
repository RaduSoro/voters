import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class ParticipantServer implements Runnable {


    private int port;
    private int loggerPort;
    private int maxParticipants;
    private int timeout;
    private ServerSocket serverSocket;
    private HashMap<String, String> properties;
    private ArrayList<SocketWrapper> clientList;
    private ArrayList<String> votingOptions;
    private ParticipantWrapper participantWrapper;
    private Socket socket;
    private boolean processNotEnded = true;
    private Thread thread;
    private int participantCounter = 0;
    public ParticipantServer(HashMap<String, String> properties, ParticipantWrapper participantWrapper) {
        this.properties = properties;
        clientList = new ArrayList<>();
        this.participantWrapper = participantWrapper;

        this.port = Integer.parseInt(properties.get(Constants.PARTICIPANT_PORT));
        this.loggerPort = Integer.parseInt(properties.get(Constants.LOGGER_PORT));
        this.maxParticipants = Integer.parseInt(properties.get(Constants.PARTICIPANTS));
        this.timeout = Integer.parseInt(properties.get(Constants.TIMEOUT));

        thread = new Thread(this, "Coordinator");
        thread.start();
    }

    public synchronized void run() {
        System.out.println("Running participant server, listening on port: " + this.port);
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (maxParticipants!=0) try {
            socket = serverSocket.accept();
            thread = new Thread(){
                @Override
                public void run() {
                    SocketWrapper socketWrapper = new SocketWrapper(socket);
                    socketWrapper.thread = thread;
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
            maxParticipants--;
        }
    }
    public void close(){
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clientList.forEach(socketWrapper -> {
            try {
                socketWrapper.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void readObject(SocketWrapper socketWrapper) {
        while (!socketWrapper.socket.isClosed() && processNotEnded){
            try {
                Object receivedObject = socketWrapper.objectInputStream.readObject();
                handleMessage((String) receivedObject,socketWrapper);
            } catch (Exception e) {
                processNotEnded = false;
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
        switch (header){
            case Constants.VOTE:
                //remove the header so we can work with the proper message
                parsedMessage.remove(0);
                //set the listening port for the socket wrapper so we know which participant sent the message
                if (participantCounter< participantWrapper.participantPorts.size()){
                    participantCounter++;
                    socketWrapper.listeningPort = parsedMessage.get(0);
                }
                try {
                    socketWrapper.thread.sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println("RECEIVED VOTES FROM "+socketWrapper.listeningPort+": " + parsedMessage);
                this.participantWrapper.parseVotes(parsedMessage,socketWrapper.listeningPort);
                break;
            default:
                System.out.println("DEFAULT ON PARTICIPANT SERVER REACHED WITH:  " + parsedMessage);
                break;
        }

    }
}
