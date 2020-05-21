import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VoteMachine {
    ParticipantWrapper participantWrapper;
    private int timeout;
    private Thread thread;
    private HashMap<String,String> previousRoundVotes;
    public VoteMachine (ParticipantWrapper participantWrapper){
        this.participantWrapper = participantWrapper;
        previousRoundVotes = new HashMap<>();
        //sleep time for the thread
        this.timeout = participantWrapper.timeout;
        startVoting();
    }
    private void startVoting (){
        //pick the first vote
        participantWrapper.logger.beginRound(1);
        String vote = participantWrapper.pickRandomVote(participantWrapper.votingOptions);
        System.out.println("MY FIRST VOTE IS: " + vote);
        //put the vote in the participant wrapper and the thread
        previousRoundVotes.put(String.valueOf(participantWrapper.listeningPort),vote);
        participantWrapper.getVotes().put(String.valueOf(participantWrapper.listeningPort),vote);
        //initializate the other ports with the null vote
        participantWrapper.participantPorts.forEach(port -> {
            previousRoundVotes.put(port,"");
            participantWrapper.getVotes().put(port,"");
        });
        //send the respective vote to the other participants
        List<Vote> votes = new ArrayList<>();
        Vote voteObject = new Vote(participantWrapper.listeningPort,vote);
        votes.add(voteObject);
        participantWrapper.sendVoteToOtherParticipants(Constants.VOTE + " "+ participantWrapper.listeningPort + " " + vote,votes);
        participantWrapper.logger.endRound(1);
        //do the rest of the voting rounds based on the timeout (N+1 ROUNDS)
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int maxRounds = participantWrapper.participantPorts.size()+1;
        for (int i = 2; i<maxRounds+2;i++){
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //have the participant pick a new vote for itself considering the new votes
            participantWrapper.logger.beginRound(i);
            System.out.println("CURRENT VOTES IN THE ROUND "+ i +" ARE " + participantWrapper.getVotes());
            System.out.println("Previous round votes" +" ARE " + previousRoundVotes);

            String currentVote = participantWrapper.pickVoteOutcome();
            System.out.println("CURRENT ROUND "+i+" OUTCOME IS " +currentVote );
            //update the vote in the map
            participantWrapper.getVotes().replace(String.valueOf(participantWrapper.listeningPort),currentVote);
            //check the votes inside the participant wrapper which are updated by the server make the difference and broadcast them further
            HashMap<String,String> newVoteMap = checkVotingDifference();
            //create the message to send to the oters
            String message = Constants.VOTE;
            votes = new ArrayList<>();
            for(String key : newVoteMap.keySet()){
                String value = newVoteMap.get(key);
                message += " " + key + " "+ value;
                voteObject = new Vote(Integer.parseInt(key),value);
                votes.add(voteObject);
            }
            participantWrapper.sendVoteToOtherParticipants(message,votes);
            participantWrapper.logger.endRound(i);
        }
        String voteOutcome = participantWrapper.pickVoteOutcome();
        List<Integer> participants = new ArrayList<>();
        participantWrapper.participantPorts.forEach(port -> participants.add(Integer.valueOf(port)));
        participantWrapper.logger.outcomeDecided(voteOutcome,participants);
        String outcome = Constants.MSG_OUTCOME + " " + voteOutcome + " " + participantWrapper.participantPorts.toString() +" "+ participantWrapper.listeningPort;
        participantWrapper.coordinatorClient.sendObject(outcome);
        participantWrapper.logger.outcomeNotified(voteOutcome,participants);
        participantWrapper.endProcess();
    }
    public HashMap<String,String> checkVotingDifference(){
        HashMap<String,String> newVoteMap = new HashMap<>();
        participantWrapper.getVotes().forEach((key,value)->{
            //if the value from the thread hashmap is different from the one in the participant add it to the newVoteMap
            if (!value.equals(previousRoundVotes.get(key))){
                newVoteMap.put(key,value);
                //update the previous round votes
                previousRoundVotes.replace(key,value);
            }
        });
        return newVoteMap;
    }

}
