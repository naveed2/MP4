package communication;

import communication.message.Messages;
import communication.message.MessagesFactory;
import membership.MemberList;
import communication.message.Messages.ProcessIdentifier;
import membership.Proc;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import static communication.message.Messages.FileIdentifier;


//This class implements the gossip

public class Gossip {

    private static final Integer NUM_OF_TARGETS = 3;
    private AtomicBoolean shouldStop;
    private long delay;
    private Proc proc;

    private static Logger logger = Logger.getLogger(Gossip.class);

    public Gossip(){
        shouldStop = new AtomicBoolean(false);
        delay = 500;
    }

//  This method is used to get the memberlist
    private MemberList getMemberList(){
        return proc.getMemberList();
    }

//  This method is used to select random processes to send them gossip messages
    public ProcessIdentifier selectRandomTarget(){
        Random rand = new Random();
        try {
            Integer randomTarget = rand.nextInt(getMemberList().size());
            return getMemberList().get(randomTarget);
        } catch (IllegalArgumentException e) {
            logger.error("empty member list ", e);
            return null;
        }
    }

//  This method start gossip thread
    public void start(){

        new Thread(new Runnable() {

            public void run() {
                while(!shouldStop.get()) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    startInfecting();
                }
            }
        }).start();

    }

//    This method start infection the processes in the system
    private void startInfecting(){
        for(Integer i = 0; i < NUM_OF_TARGETS; i++){
            ProcessIdentifier infectedProcess = selectRandomTarget();
            if(infectedProcess == null) {
                return;
            }
            if(notSelf(infectedProcess)) {
                sendSyncMessage(infectedProcess);
                sendSyncFileListMessage(infectedProcess);
            }
        }
    }

//    This method checks if the argument ProcessIdentifier refers to this process or not
    private boolean notSelf(ProcessIdentifier identifier) {
        return !proc.getId().equals(identifier.getId());
    }

//    This method sends syncs messages to sync memberlist
    private void sendSyncMessage(ProcessIdentifier process){
        UDPClient udpClient = new UDPClient(process);
        Messages.Message message = MessagesFactory.generateSyncProcessMessage(
                proc.getTimeStamp(), proc.getIdentifier(), proc.getMemberList());
        udpClient.sendMessage(message.toByteArray());
    }

    private List<FileIdentifier> fidList = new LinkedList<FileIdentifier>();
    private static final Integer MAX_NUM_FILES_TO_SYNC = 30;

//    This method sends syncs message to sync filelist
    private void sendSyncFileListMessage(ProcessIdentifier remoteProcess) {
        List<FileIdentifier> filesNeedToSync;
        int numToSync;

        if(fidList.size() == 0) {
            fidList = proc.getSDFS().getFileList().getList();
        }
        numToSync = Math.min(fidList.size(), MAX_NUM_FILES_TO_SYNC);

        filesNeedToSync = fidList.subList(0, numToSync);
        fidList = fidList.subList(numToSync, fidList.size());

        Messages.Message message = MessagesFactory.generateSyncFileListMessage(
                filesNeedToSync, proc.getTimeStamp(),proc.getIdentifier(), proc.getSDFS());
        UDPClient udpClient = new UDPClient(remoteProcess);
        udpClient.sendMessage(message.toByteArray());
    }

    public void stop() {
        shouldStop.set(true);
        Thread.interrupted();
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }

}
