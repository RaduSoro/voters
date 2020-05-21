import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.TimeoutException;

public class UDPLoggerClient {
	
	private final int loggerServerPort;
	private final int processId;
	private final int timeout;

	/**
	 * @param loggerServerPort the UDP port where the Logger process is listening o
	 * @param processId the ID of the Participant/Coordinator, i.e. the TCP port where the Participant/Coordinator is listening on
	 * @param timeout the timeout in milliseconds for this process 
	 */
	public UDPLoggerClient(int loggerServerPort, int processId, int timeout) {
		this.loggerServerPort = loggerServerPort;
		this.processId = processId;
		this.timeout = timeout;
	}
	
	public int getLoggerServerPort() {
		return loggerServerPort;
	}

	public int getProcessId() {
		return processId;
	}
	
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Sends a log message to the Logger process
	 * 
	 * @param message the log message
	 * @throws IOException
	 */
	public void logToServer(String message) throws IOException {
		int counter = 0;
		boolean receivedAck = false;
        DatagramSocket ds = new DatagramSocket();
        InetAddress ip = InetAddress.getLocalHost();
        byte[] bytes = message.getBytes();
        //BUF IS BYTE CONVERT THAT STRING TO BYTES
        DatagramPacket DpSend = new DatagramPacket(bytes, bytes.length, ip, loggerServerPort);
        while (counter<3 && !receivedAck){
            ds.send(DpSend);
		    try {
		        ds.setSoTimeout(timeout);
                byte[] receive = new byte[65535];
                DatagramPacket DpReceive = new DatagramPacket(receive, receive.length);
                ds.receive(DpReceive);
//                System.out.println("Client received data:-" + data(receive));
                receivedAck = true;
            } catch (SocketException e) {
                counter++;
            }
        }
		if (counter == 3 && !receivedAck) {
            throw new IOException("LOGGER SERVER FAILED TO ACK MESSAGE: " + message);
        }
	}

    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }
}
