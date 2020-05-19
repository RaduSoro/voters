import java.util.HashMap;

public class mainParticipant {
    public static void main(String[] args) {
        //        <cport> <lport> <pport> <timeout>
        HashMap<String,String> properties = new HashMap<>();
        properties.put(Constants.COORDINATOR_PORT, args[0]);
        properties.put(Constants.LOGGER_PORT, args[1]);
        properties.put(Constants.PARTICIPANT_PORT, args[2]);
        properties.put(Constants.TIMEOUT, args[3]);

        ParticipantWrapper participantWrapper = new ParticipantWrapper(properties);
    }
}
