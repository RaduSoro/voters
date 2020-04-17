import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class VoterWrapper {
    private  Client coordinatorClient;
    public  HashMap<String,Client> clients;
    private Server participantServer;
    private ArrayList<String> args;

    public VoterWrapper(String[] args){
        this.args = new ArrayList<>(Arrays.asList(args));
        coordinatorClient = new Client(args,this);
    }

    public void initiateInfrastucture(ArrayList<String> parsedTokens){
        clients = new HashMap<>();
        clients.put("coordinatorClient",coordinatorClient);
    }
}
