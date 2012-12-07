package communication;

import communication.message.Messages;
import membership.Proc;
import misc.MiscTool;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class TCPClient {

    private String remoteIP;
    private Integer remotePort;
    private TCPConnection tcpConnection;
    private Proc proc;

    private static Logger logger = Logger.getLogger(TCPClient.class);

    public TCPClient(String remoteAddress) {
        if(!MiscTool.isIPAddress(remoteAddress)) {
            throw new IllegalArgumentException("Wrong address format");
        }
        String str[]  = remoteAddress.split(":");
        remoteIP = str[0];
        remotePort = Integer.parseInt(str[1]);
    }

//    this method set parameters for the remote host
    public TCPClient(String remoteIP, Integer remotePort) {
        this.remoteIP = remoteIP;
        this.remotePort = remotePort;
    }

//  this method conencts to the remote host
    public boolean connect() {
        try {
            Socket socket = new Socket(remoteIP, remotePort);
            tcpConnection = new TCPConnection();
            tcpConnection.setSocket(socket).setProc(proc);
        } catch (IOException e) {
            if(e.getMessage().equals("Connection refused")) {
                logger.info("connect(): socket connection refused");
            } else {
                logger.error("socket construction error", e);
            }
            return false;
        }
        return true;
    }

//    this method closes the socket
    public void close() {
        try {
            tcpConnection.close();
        } catch (IOException e) {
            logger.error("socket close error", e);
        }
    }

//    this method is used when we want to send an Integer, underlying program will change it to bytes accordingly
    public void sendData(int b) {
        tcpConnection.sendData(b);
    }
//  this method is used to send data from the input stream
    public void sendData(InputStream is) {
        tcpConnection.sendData(is);
    }

    public void sendData(byte[] bytes) {
        tcpConnection.sendData(bytes);
    }

    public void receiveAndSaveData(String localFilepath){
        tcpConnection.receiveAndSaveData(localFilepath);
    }

    public void sendData(String str) {
        sendData(str.getBytes());
    }

    public void sendData(Messages.Message m) {
        sendData(m.toByteArray());
    }

    public void setProc(Proc proc) {
        if(proc == null) {
            throw new NullPointerException("null argument!");
        }
        this.proc = proc;
    }
}
