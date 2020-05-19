import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.CheckedOutputStream;

public class mainCoordinator {
    public static void main(String[] args) {
        //        〈port〉 〈lport〉 〈parts〉 〈timeout〉[〈option〉]
        HashMap<String,String> properties = new HashMap<>();
        properties.put(Constants.PORT, args[0]);
        properties.put(Constants.LOGGER_PORT,args[1]);
        properties.put(Constants.PARTICIPANTS, args[2]);
        properties.put(Constants.TIMEOUT, args[3]);
        ArrayList<String> votingOptions = new ArrayList<>();
        for (int i = 4; i < args.length ; i++) {
            votingOptions.add(args[i]);
        }

        CoordinatorWrapper coordinatorWrapper = new CoordinatorWrapper(properties,votingOptions);
    }
}
