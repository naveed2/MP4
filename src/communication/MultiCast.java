package communication;

import communication.message.Messages;
import membership.Proc;
import org.apache.log4j.Logger;

import java.util.Collection;

import static communication.message.Messages.Message;
import static communication.message.Messages.ProcessIdentifier;

public class MultiCast {

    private static Proc proc;
    private static Logger logger = Logger.getLogger(MultiCast.class);

    private MultiCast() {
    }

    private Integer num;
    private Thread curTread;

    public void setNum(Integer num ) {
        this.num = num;
        curTread = Thread.currentThread();
    }

    public static void init(Proc proc) {
        MultiCast.proc = proc;
    }

    public static void broadCast(Collection<ProcessIdentifier> procIDs, final Message m) {
        final MultiCast multiCast = new MultiCast();
        multiCast.setNum(procIDs.size());

        for(final ProcessIdentifier procID : procIDs) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TCPClient tcpClient = new TCPClient(procID);
                    tcpClient.setProc(proc);
                    if(tcpClient.connect()) {
                        tcpClient.sendData(m);
                        tcpClient.close();
                    } else {
                        logger.error("BroadCast error");
                    }

                    multiCast.onFinish();
                }
            }).start();
        }

        try {
            Thread.sleep(25000);
        } catch (InterruptedException e) {
            //
        }
    }

    private void onFinish() {
        num--;
        if(num==0) {
            curTread.interrupt();
        }
    }
}
