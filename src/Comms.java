import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Comms implements Runnable {
    private int port;
    private int loggerPort;
    private int maxParticipants;
    private int timeout;
    private ArrayList<String> options;

    private int cordinatorPort;
    private boolean foundHost = false;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;

    protected ServerSocket serverSocket = null;

    private ArrayList<String> arguments;
    private Socket socket;
    private Thread thread;
    private String type;
    private HashMap<Socket, ClientHandler> socketThreadHashMap;

    public Comms (String type, String[] arguments){
        this.type = type;
        this.arguments = new ArrayList<>(Arrays.asList(arguments));
        socketThreadHashMap = new HashMap<>();
        this.init();
    }

    public void init(){
        switch (this.type){
            case MSG.SERVER:
                initServer();
            break;

            case MSG.LOGGER:
            break;

            case MSG.VOTER:
                initParticipant();
            break;
        }
    }

    private void initServer(){
        this.port = Integer.parseInt(this.arguments.get(0));
        this.loggerPort = Integer.parseInt(this.arguments.get(1));
        this.maxParticipants = Integer.parseInt(this.arguments.get(2));
        this.maxParticipants = Integer.parseInt(this.arguments.get(3));
        //clear the assigments
        for (int i = 0; i < 4; i++) {
            this.arguments.remove(0);
        }
        this.options = new ArrayList<>(this.arguments);
        thread = new Thread(this, "server");
        thread.start();
    }
    private void initParticipant(){
        this.cordinatorPort = Integer.parseInt(this.arguments.get(0));
        this.loggerPort = Integer.parseInt(this.arguments.get(1));
        this.port = Integer.parseInt(this.arguments.get(2));
        this.timeout = Integer.parseInt(this.arguments.get(3));
        thread = new Thread(this, "Participant " + this.port);
        thread.start();
        connectAsClient();
    }

    private void connectAsClient(){
        while (!this.foundHost) {
            try {
                socket = new Socket("127.0.0.1", this.cordinatorPort);
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

    public synchronized void run() {
        System.out.println("Running thread server" + Thread.currentThread());
        try {
            serverSocket = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) try {
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
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException ie) {
            ie.printStackTrace();
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

class ClientHandler {
    Thread thread;
    Socket socket;
    private boolean status = false;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    public ClientHandler(Thread thread, Socket socket) {
        this.thread = thread;
        this.socket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean getExitStatus(){
        return this.status;
    }
    public void setExitStatus(boolean status){
        this.status = status;
    }

    public void closeStreams(){
        try {
            this.objectOutputStream.close();
            this.objectInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeThread(){
        this.thread = null;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return this.objectOutputStream;
    }

    public ObjectInputStream getObjectInputStream() {
        return this.objectInputStream;
    }

}
