import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CoordinatorWrapper {
    private  ArrayList<String> votingOptions;
    private HashMap<String,String> properties;
    private CoordinatorServer coordinatorServer;
    CoordinatorLogger coordinatorLogger;

    public CoordinatorWrapper(HashMap<String, String> properties, ArrayList<String> votingOptions) {
        this.properties = properties;
        this.votingOptions = votingOptions;
        int loggerPort = Integer.parseInt(properties.get(Constants.LOGGER_PORT));
        int listeningPort = Integer.parseInt(properties.get(Constants.PORT));
        int timeout = Integer.parseInt(properties.get(Constants.TIMEOUT));
//        UDPLoggerClient udpLoggerClient = new UDPLoggerClient(loggerPort,listeningPort,timeout);
//        try {
//            udpLoggerClient.logToServer("HELLO");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            CoordinatorLogger.initLogger(loggerPort,listeningPort,timeout);
        } catch (IOException e) {
            e.printStackTrace();
        }
        coordinatorLogger = CoordinatorLogger.getLogger();
        startCoordinatorServer();
    }

    private void startCoordinatorServer(){
        coordinatorServer = new CoordinatorServer(properties,votingOptions, coordinatorLogger);
    }
}
