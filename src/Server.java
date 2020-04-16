import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Server implements Runnable {

    private int port;
    private HashMap<Socket, ClientHandler> socketThreadHashMap;
    protected ServerSocket serverSocket = null;
    private Socket socket;
    private Thread thread;
    private int loggerPort;
    private int maxParticipants;
    private int timeout;
    private ArrayList<String> options;
    private ArrayList<String> arguments;

    public Server (String[] arguments){
        this.arguments = new ArrayList<>(Arrays.asList(arguments));
        socketThreadHashMap = new HashMap<>();
        this.init();
    }


    public void init(){
        this.port = Integer.parseInt(this.arguments.get(0));
        this.loggerPort = Integer.parseInt(this.arguments.get(1));
        this.maxParticipants = Integer.parseInt(this.arguments.get(2));
        this.timeout = Integer.parseInt(this.arguments.get(3));
        //clear the assigments
        for (int i = 0; i < 4; i++) {
            this.arguments.remove(0);
        }
        this.options = new ArrayList<>(this.arguments);
        thread = new Thread(this, "server");
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
                    sendInitialData(socket);
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
            maxParticipants--;
            if (maxParticipants == 0){
                final String[] clientDetails = {MSG.DETAILS};
                String voteOpt = MSG.VOTE_OPTIONS;
                for (String option: options) {
                    voteOpt += " " + option;
                }
                socketThreadHashMap.forEach((socket1, clientHandler) -> {
                    clientDetails[0] += " "+socket1.getLocalPort();
                });
                broadcast(clientDetails[0]);
                broadcast(voteOpt);
            }
                System.out.println(maxParticipants);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void readObject(Socket socket, ObjectInputStream objectInputStream) {
        ClientHandler clientHandler = socketThreadHashMap.get(socket);
        while (!clientHandler.getExitStatus() && !socket.isClosed()) {
            try {
                Object o = objectInputStream.readObject();
                handleClient(o,socket);
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
    public void handleClient(Object o, Socket socket){
        System.out.println(o);
    }

}
