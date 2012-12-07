package communication;

/**
 *
 */
import communication.message.Messages.ProcessIdentifier;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;

public class UDPClient {

    private ProcessIdentifier processIdentifier;
    private Integer udpPort;
    private static Logger logger = Logger.getLogger(UDPClient.class);

    public UDPClient(ProcessIdentifier processIdentifier) {
        this.processIdentifier = processIdentifier;
        udpPort = processIdentifier.getPort() + 1;  // UDPPort = TCPPort + 1
    }

    public void sendMessage(byte[] bytes) {
        InetAddress address;
        try {
            address = InetAddress.getByName(processIdentifier.getIP());
        } catch (UnknownHostException e) {
            logger.error("unknown host " + e);
            return;
        }
        DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, udpPort);

        DatagramSocket socket;
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            logger.error("couldn't bind udp port " + e);
            return;
        }

        try {
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            logger.error("fail to send udp packet " + e);
        }
    }

    public void sendMessage(String str) {
        sendMessage(str.getBytes());
    }

    public void sendMessage(ProcessIdentifier processIdentifier){

    }
}
