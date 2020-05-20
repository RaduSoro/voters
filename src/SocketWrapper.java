import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketWrapper {
    public ObjectInputStream objectInputStream;
    public ObjectOutputStream objectOutputStream;
    public Thread thread;
    public Socket socket;
    public String listeningPort;
    public SocketWrapper(Socket socket){
        this.socket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
