package communication;

import communication.message.Messages;
import membership.Proc;

import java.util.Collection;

import static communication.message.Messages.Message;
import static communication.message.Messages.ProcessIdentifier;

public class MultiCast {

    private static Proc proc;

    private MultiCast() {

    }

    public static void init(Proc proc) {
        MultiCast.proc = proc;
    }

    public static void broadCast(Collection<ProcessIdentifier> procIDs, final Message m) {
        for(final ProcessIdentifier procID : procIDs) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String remoteAddress = procID.getIP() + ":" + procID.getPort();
                    TCPClient tcpClient = new TCPClient(remoteAddress);
                    tcpClient.setProc(proc);
                    if(tcpClient.connect()) {
                        tcpClient.sendData(m);
                        tcpClient.close();
                    }
                }
            }).start();
        }

    }
}
