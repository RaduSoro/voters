import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ParticipantWrapper {
    private HashMap<String, String> properties;
    public CoordinatorClient coordinatorClient;
    public ArrayList<String> participantPorts;
    public ArrayList<String> votingOptions;
    private HashMap<String,String> votes;
    public ArrayList<ParticipantClient> participantClients;
    public int listeningPort;
    public int timeout;
    public ParticipantServer participantServer;
    public ParticipantLogger logger;
    ArrayList<String> crashedPorts = new ArrayList<>();
    public synchronized HashMap<String, String> getVotes() {
        return votes;
    }

    public ParticipantWrapper(HashMap<String, String> properties, ParticipantLogger logger) {
        this.properties = properties;
        this.votes = new HashMap<>();
        participantPorts = new ArrayList<>();
        participantClients = new ArrayList<>();
        votingOptions = new ArrayList<>();
        this.logger = logger;
        this.listeningPort = Integer.parseInt(properties.get(Constants.PARTICIPANT_PORT));
        this.timeout = Integer.parseInt(properties.get(Constants.TIMEOUT));
        connectToCoordinator();
    }

    public void connectToCoordinator(){
        coordinatorClient = new CoordinatorClient(properties,this);
    }

    public synchronized void parseVotes (ArrayList<String> vote, String port){
        List<Vote> rawVotes = new ArrayList<>();
        Vote voteObj  = null;
        while (!vote.isEmpty()){
            String votePort = vote.get(0);
            String voteLetter = vote.get(1);
            if (voteLetter!= ""){
                if (votePort.equals(this.listeningPort)){
                    voteObj = new Vote(Integer.parseInt(vote.get(0)),vote.get(1));
                    rawVotes.add(voteObj);
                    vote.remove(0);
                    vote.remove(0);
                } else if(!crashedPorts.contains(port) && port.equals(votePort)){
                    this.getVotes().put(votePort,voteLetter);
                    voteObj = new Vote(Integer.parseInt(vote.get(0)),vote.get(1));
                    rawVotes.add(voteObj);
                    vote.remove(0);
                    vote.remove(0);
                } else if (crashedPorts.contains(votePort)){
                    this.getVotes().put(votePort,voteLetter);
                    voteObj = new Vote(Integer.parseInt(vote.get(0)),vote.get(1));
                    rawVotes.add(voteObj);
                    vote.remove(0);
                    vote.remove(0);
                }
                else {
                    voteObj = new Vote(Integer.parseInt(vote.get(0)),vote.get(1));
                    rawVotes.add(voteObj);
                    vote.remove(0);
                    vote.remove(0);
                }
            }else{
                voteObj = new Vote(Integer.parseInt(vote.get(0)),vote.get(1));
                rawVotes.add(voteObj);
                vote.remove(0);
                vote.remove(0);
            }
        }
        logger.votesReceived(Integer.parseInt(port),rawVotes);
    }
    public synchronized void removeParticipant (ParticipantClient participantClient){
        participantClients.remove(participantClient);
    }

        //no of participants for the max connections allowed on the participant server
    public void initiateInfrastructure() {
        properties.put(Constants.PARTICIPANTS, String.valueOf(participantPorts.size()));
        //makes new server
        participantServer = new ParticipantServer(this.properties,this);
        //create clients to connect to the other participants in order to send messages to them
        for (String port : participantPorts) {
//            votes.put(port,"");
            ParticipantClient participantClient = new ParticipantClient(port,this);
            participantClients.add(participantClient);
        }
        startVoting();
    }

    public void endProcess(){
        participantServer.close();
        try {
            coordinatorClient.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startVoting(){
        VoteMachine voteMachine = new VoteMachine(this);
    }

    public void sendVoteToOtherParticipants(String vote, List<Vote> rawVotes){
        participantClients.forEach(client -> {
            client.sendObject(vote);
            logger.votesSent(client.participantServerPort,rawVotes);
        });
    }

    public String pickRandomVote (ArrayList<String> vote){
//        if (listeningPort%2 == 0) return "C";
//        else return "A";

        int voteNumber = ThreadLocalRandom.current().nextInt(0,votingOptions.size());
        return vote.get(voteNumber);
    }

    //Receives an Arraylist of the votes e.g [A,A,B,C,B]
    // picks the majority by count if there is a tie, pick the first in alphabetical order
    public String pickVoteOutcome(){
        ArrayList<String> voteValues = new ArrayList<>();
        this.votes.forEach((k,v) -> {
            if (!v.equals(""))voteValues.add(v);
        });
        Set<String> set = new HashSet<>(voteValues);
        HashMap<String,Integer> counter = new HashMap<>();
        //counter will be a,5 etc
        for (String element: set) {
            counter.put(element, Collections.frequency(voteValues,element));
        }
        ArrayList<Integer> values = new ArrayList<>();
        for (String key: counter.keySet()) {
            values.add(counter.get(key));
        }
        //Sort the array descending order
        Collections.sort(values, Collections.reverseOrder());
        Integer maxValue = values.get(0);//Get the highest number
        ArrayList<Integer> maxValues = new ArrayList<>();
        // put the highest values in the array
        ArrayList<String> outcome = new ArrayList<>();
        for (String key: counter.keySet()) {
            Integer value = counter.get(key);
            if (value.intValue() == maxValue.intValue()) outcome.add(key);
        }
        //sort the outcome alphabetically
        Collections.sort(outcome);
        //return the first ITEM
        return outcome.get(0);
    }
}
