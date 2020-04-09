import java.util.ArrayList;
import java.util.Arrays;

public class Coordinator {
    public static void main(String[] args) {
//        〈port〉 〈lport〉 〈parts〉 〈timeout〉[〈option〉]
        ArrayList<String> options = new ArrayList<>(Arrays.asList(args));
        System.out.println("Starting coordinator with the options: ");
        System.out.println("Port: "+args [0]);
        System.out.println("Logger port: "+args [1]);
        System.out.println("Timeout: "+args [2]);
        System.out.println("Options: ");
        options.remove(0);
        options.remove(0);
        options.remove(0);
        System.out.print(options);
    }
}
