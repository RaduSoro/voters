import java.io.IOException;
import java.util.HashMap;

public class Participant {
    public static void main(String[] args) {
        //        <cport> <lport> <pport> <timeout>
        HashMap<String,String> properties = new HashMap<>();
        properties.put(Constants.COORDINATOR_PORT, args[0]);
        properties.put(Constants.LOGGER_PORT, args[1]);
        properties.put(Constants.PARTICIPANT_PORT, args[2]);
        properties.put(Constants.TIMEOUT, args[3]);

        int loggerPort = Integer.parseInt(properties.get(Constants.LOGGER_PORT));
        int listeningPort = Integer.parseInt(properties.get(Constants.PARTICIPANT_PORT));
        int timeout = Integer.parseInt(properties.get(Constants.TIMEOUT));
        ParticipantLogger logger;
        try {
            ParticipantLogger.initLogger(loggerPort,listeningPort,timeout);
        } catch (IOException e) {
//            System.out.println("GETTING LOGGER INSTANCE");
        }
            logger = ParticipantLogger.getLogger();
        ParticipantWrapper participantWrapper = new ParticipantWrapper(properties, logger);
    }
}
