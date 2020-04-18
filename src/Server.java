import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Server implements Runnable {

    private int port;
    private HashMap<Socket, ClientHandler> socketThreadHashMap;
    protected ServerSocket serverSocket = null;
    private Socket socket;
    private Thread thread;
    private int loggerPort;
    private int maxParticipants;
    private String serverType;
    private int timeout;
    private ArrayList<String> options;
    private ArrayList<String> arguments;
    private ArrayList<String> ports;

    public Server (ArrayList<String> arguments, String type){
        this.arguments = arguments;
        this.serverType = type;
        ports = new ArrayList<>();
        socketThreadHashMap = new HashMap<>();
        if (type.equals(MSG.COORDINATOR)) this.initCoordinatorServer();
        else this.initClientServer();
    }

    public void initClientServer(){
        System.out.println(arguments);
        this.loggerPort = Integer.parseInt(this.arguments.get(1));
        this.port = Integer.parseInt(this.arguments.get(2));
        this.timeout = Integer.parseInt(this.arguments.get(3));
        this.maxParticipants = arguments.size()-4;
        thread = new Thread(this, "ParticipantServer");
        thread.start();
    }

    public void initCoordinatorServer(){
        this.port = Integer.parseInt(this.arguments.get(0));
        this.loggerPort = Integer.parseInt(this.arguments.get(1));
        this.maxParticipants = Integer.parseInt(this.arguments.get(2));
        this.timeout = Integer.parseInt(this.arguments.get(3));
        //clear the assigments
        for (int i = 0; i < 4; i++) {
            this.arguments.remove(0);
        }
        this.options = new ArrayList<>(this.arguments);
        thread = new Thread(this, "Coordinator");
        thread.start();
    }

    public synchronized void run() {
        System.out.println("Running thread server" + Thread.currentThread());
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (maxParticipants!=0) try {
            socket = serverSocket.accept();
            System.out.println("Acquired new client " + socket.getLocalPort());
            thread = new Thread() {
                @Override
                public void run() {
                    //creates the handler with the I/O object stream
                    ClientHandler clientHandler = new ClientHandler(this, socket);
                    socketThreadHashMap.put(socket, clientHandler);
//                    sendInitialData(socket);
                    readObject(socket, clientHandler.getObjectInputStream());
                    socketThreadHashMap.remove(socket, clientHandler);
                    clientHandler = null;
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    this.interrupt();
                }
            };
            thread.start();
            Thread.sleep(1000);
            if (this.serverType.equals(MSG.COORDINATOR))setupVote();
            else startupVote();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startupVote() {
        System.out.println("STARTING VOTE");
    }

    public void setupVote(){
        maxParticipants--;
        if (maxParticipants == 0 && this.serverType.equals(MSG.COORDINATOR)){
            String voteOpt = MSG.VOTE_OPTIONS;
            for (String option: options) {
                voteOpt += " " + option;
            }
            for(Map.Entry<Socket, ClientHandler> entry : socketThreadHashMap.entrySet()) {
                Socket client = entry.getKey();
                ClientHandler clientHandler = entry.getValue();
                String options = MSG.DETAILS;
                for (String port : ports){
                    if (!port.equals(clientHandler.getPort())) options += " "+port;
                }
                sendObject(options,client);
            }
            broadcast(voteOpt);
        }
    }
    public void readObject(Socket socket, ObjectInputStream objectInputStream) {
        ClientHandler clientHandler = socketThreadHashMap.get(socket);
        while (!clientHandler.getExitStatus() && !socket.isClosed()) {
            try {
                Object o = objectInputStream.readObject();
                handleClient((String) o,socket);
            } catch (Exception e) {
                clientHandler.closeStreams();
                clientHandler.closeThread();
                clientHandler.setExitStatus(true);
                System.out.println("Connection with client lost");
            }
        }
    }
    public void sendObject(Object o, Socket socket) {
        if (socket == null || socket.isClosed()) return;
        try {
            ObjectOutputStream objectOutputStream = socketThreadHashMap.get(socket).getObjectOutputStream();
            objectOutputStream.writeObject(o);
            objectOutputStream.flush();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    public void broadcast(Object o) {
        if (!socketThreadHashMap.isEmpty()){
            socketThreadHashMap.keySet().forEach((socket) -> {
                sendObject(o, socket);
            });
        }
    }
    public void sendInitialData(Socket socket){
        sendObject("Server hellos back",socket);
    }
    public void handleClient(String tokens, Socket socket){
        ArrayList<String> parsedTokens =   new ArrayList<>(Arrays.asList(tokens.split(" ")));
        String header = parsedTokens.get(0);
        switch (header){
            case MSG.JOIN:
                this.ports.add(parsedTokens.get(1));
                socketThreadHashMap.get(socket).setPort(parsedTokens.get(1));
            break;
            default:
                System.out.println(tokens);
            break;
        }

    }

}
