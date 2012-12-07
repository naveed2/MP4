package communication;

import communication.message.MessagesFactory;
import membership.Proc;

import java.util.concurrent.atomic.AtomicBoolean;

import static communication.message.Messages.Message;
import static communication.message.Messages.ProcessIdentifier;

public class FailureDetector {

    private Proc proc;
    private ProcessIdentifier sendTo, listenFrom;
    private AtomicBoolean shouldStopSend, shouldStopListen;
    private Thread listenThread;
    private Integer suspension;

    private static final Integer HEART_BEATING_SEND_DELAY = 200;
    private static final Integer HEART_BEATING_LISTEN_DELAY = 1000;
    private static final Integer MAXIMUM_SUSPENSION = 5;

    public FailureDetector() {
        shouldStopListen = new AtomicBoolean(false);
        shouldStopSend = new AtomicBoolean(false);
        sendTo = listenFrom = null;
        suspension = 0;
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }

    public void start() {
        startSendHeartBeating();
        startListen();
    }

    public void startSendHeartBeating() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!shouldStopSend.get()) {
                    try{
                        Thread.sleep(HEART_BEATING_SEND_DELAY);
                    } catch(InterruptedException e) {
                        //do nothing
                    }
                    sendHeartBeatingMessage();
                }
            }
        }).start();
    }

    private void startListen() {
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!shouldStopListen.get()) {
                    try{
                        Thread.sleep(HEART_BEATING_LISTEN_DELAY);
                        if(listenFrom == null) {
                            continue;
                        }
                        ++suspension;
                        if(suspension >= MAXIMUM_SUSPENSION) {
                            proc.getMemberList().setAsToBeDeleted(listenFrom);
                            stopListen();
                        }
                    } catch (InterruptedException e) {
                        suspension = 0;
                    }
                }
            }
        });

        listenThread.start();
    }

    public void onReceivingHeartBeat() {
        listenThread.interrupt();
    }

    public void sendHeartBeatingMessage() {
        if(sendTo == null) {
            return;
        }
        UDPClient udpClient = new UDPClient(sendTo);
        Message heartBeatMessage = MessagesFactory.generateHearBeatMessage(proc.getTimeStamp(), proc.getIdentifier());
        udpClient.sendMessage(heartBeatMessage.toByteArray());
    }

    public void stop() {
        stopSend();
        stopListen();
    }

    public void stopSend() {
        shouldStopSend.set(true);
    }

    public void stopListen() {
        shouldStopListen.set(true);
    }

    public FailureDetector setSendToMachine(ProcessIdentifier sendToMachine) {
        sendTo = sendToMachine;
        if(shouldStopListen.get()) {
            shouldStopListen.set(false);
            listenThread.start();
        }
        return this;
    }

    public FailureDetector setListenFromMachine(ProcessIdentifier listenFromMachine) {
        listenFrom = listenFromMachine;
        return this;
    }
}
