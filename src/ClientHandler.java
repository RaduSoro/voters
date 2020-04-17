import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class ClientHandler {
    Thread thread;
    Socket socket;
    private boolean status = false;
    private ObjectOutputStream objectOutputStream = null;
    private ObjectInputStream objectInputStream = null;
    private String port;
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

    public void setPort (String port){
        this.port = port;
    }

    public String getPort(){
        return this.port;
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
