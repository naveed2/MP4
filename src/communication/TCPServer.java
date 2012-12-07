package communication;


import membership.Proc;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This TCPServer is used to communicate clients with message controlling packets.
 */
public class TCPServer {
    private Integer port;
    private ServerSocket serverSocket;
    private Socket socket;
    private Logger logger = Logger.getLogger(TCPServer.class);
    private AtomicBoolean shouldStop;
    private Proc proc;

    public TCPServer(Integer port) {
        this.port = port;
        this.shouldStop = new AtomicBoolean(false);
    }

    public boolean start() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }

        new Thread(new Runnable() {
            public void run() {
                startListening();
            }
        }).start();

        return true;
    }

    private void startListening() {
        while(!shouldStop.get()) {
            try {
                socket = serverSocket.accept();
                TCPConnection conn = new TCPConnection();
                conn.setSocket(socket).setProc(proc);
                handleConnection(conn);
            } catch (IOException e) {
                logger.error("server socket exception", e);
            }
        }
    }

    private void handleConnection(final TCPConnection TCPConnection) {
        new Thread( new Runnable() {
            public void run() {
                while(!shouldStop.get()) {
                    TCPConnection.startReceiving();
                }
            }
        }).start();
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }

    public void stop() {
        shouldStop.set(true);
    }
}
