import java.util.ArrayList;
import java.util.Arrays;

public class Coordinator {
    public static void main(String[] args) {
//        〈port〉 〈lport〉 〈parts〉 〈timeout〉[〈option〉]
//        ArrayList<String> options = new ArrayList<>(Arrays.asList(args));
        Comms comms = new Comms(MSG.SERVER, args);
        System.out.println("Starting coordinator with the options: ");
        System.out.println("Port: "+args [0]);
        System.out.println("Logger port: "+args [1]);
        System.out.println("Participants: "+args [2]);
        System.out.println("Timeout: "+args [3]);
        System.out.print("Options: ");
        for (int i = 4; i < args.length ; i++) {
            System.out.print(args[i]+ " ");
        }

    }
}
