import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class VoterWrapper {
    private  Client coordinatorClient;
    public  HashMap<String,Client> clients;
    private Server participantServer;
    private ArrayList<String> args;
    private Client voter;
    private ArrayList<String> details;

    public VoterWrapper(String[] args){
        this.details = new ArrayList<>();
        this.args = new ArrayList<>(Arrays.asList(args));
        coordinatorClient = new Client(this.args,this, MSG.COORDINATOR);
    }

    public void initiateInfrastucture(ArrayList<String> parsedTokens){
        clients = new HashMap<>();
        clients.put("coordinatorClient",coordinatorClient);
        args.addAll(parsedTokens);
        Server particiPantServer = new Server(args, MSG.CLIENT);
        System.out.println("PARSED TOKENS : " + parsedTokens);
        // initiate and connect clients
        parsedTokens.remove(0);
        this.args.addAll(details);
        for (String token : details){
            voter = new Client(this.args,this,MSG.VOTER);
        }
    }

    public ArrayList<String> getDetails(){
        return this.details;
    }
}
