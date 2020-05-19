import java.util.ArrayList;
import java.util.HashMap;

public class CoordinatorWrapper {
    private  ArrayList<String> votingOptions;
    private HashMap<String,String> properties;
    private CoordinatorServer coordinatorServer;

    public CoordinatorWrapper(HashMap<String, String> properties, ArrayList<String> votingOptions) {
        this.properties = properties;
        this.votingOptions = votingOptions;
        startCoordinatorServer();
    }

    private void startCoordinatorServer(){
        coordinatorServer = new CoordinatorServer(properties,votingOptions);
    }
}
