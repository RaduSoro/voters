import java.util.ArrayList;
import java.util.Arrays;

public class UDPLoggerServer {
    public static void main(String[] args) {
//        〈port〉 〈lport〉 〈parts〉 〈timeout〉[〈option〉]
        ArrayList<String> options = new ArrayList<>(Arrays.asList(args));
        System.out.println("Port: "+args [0]);
    }
}
