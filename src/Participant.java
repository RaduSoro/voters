import java.util.ArrayList;
import java.util.Arrays;

public class Participant {
    public static void main(String[] args) {
//        <cport> <lport> <pport> <timeout>
        Client comms = new Client(args);
        ArrayList<String> options = new ArrayList<>(Arrays.asList(args));
        System.out.println("Starting client with the options: ");
        System.out.println("Coordinator Port: "+args [0]);
        System.out.println("Logger port: "+args [1]);
        System.out.println("Participant Port: "+args [2]);
        System.out.println("Timeout: "+args [3]);
    }
}
