package maplejuice;

import communication.MultiCast;
import communication.ProcessIdentifierFactory;
import communication.TCPClient;
import communication.message.Messages;
import communication.message.MessagesFactory;
import membership.AbstractProcFailureListener;
import membership.PIDComparator;
import membership.Proc;
import misc.MiscTool;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import static communication.message.Messages.Message;
import static communication.message.Messages.ProcessIdentifier;

/**
 * Maple master. Responsible for assigning files to procs.
 */
public class MapleForMaster {

    private static Logger logger = Logger.getLogger(MapleForMaster.class);
    private Proc proc;

    private String cmdExe;
    private String filePrefix;
    private List<String> inputFiles;
    private List<Messages.ProcessIdentifier> procIDs;

    private Map<String, List<String>> assignedFiles;

    private AbstractProcFailureListener failureListener;

    public MapleForMaster() {

    }

    public MapleForMaster setProc(Proc proc) {
        this.proc = proc;
        return this;
    }

    public void run(String cmdExe, String filePrefix, List<String> files) {

        this.cmdExe = cmdExe;
        this.filePrefix = filePrefix;
        inputFiles = files;

        procIDs = proc.getMemberList().getList();
        Collections.sort(procIDs, new PIDComparator());

        //broadcast master
        MultiCast.broadCast(
                procIDs,
                MessagesFactory.generateMasterMessage(proc.getIdentifier()));

        //waiting time for broadcast
        MiscTool.sleep(1000);

        //assignFiles
        assignFiles();

        //send message
        sendMapleMessage();
    }

    public void assignFiles() {
        assignedFiles = new HashMap<String, List<String>>();

        Iterator<ProcessIdentifier> iterator = procIDs.iterator();

        for(String file : inputFiles) {
            if(!iterator.hasNext()) {
                iterator = procIDs.iterator();
            }

            ProcessIdentifier curProc = iterator.next();
            String key = curProc.getId();

            assignFile(key, file);
        }
    }

    public void assignFile(String id, String file) {
        synchronized (this) {
            if(assignedFiles.containsKey(id)) {
                List<String> fileList = assignedFiles.get(id);
                fileList.add(file);
            } else {
                LinkedList<String> fileList = new LinkedList<String>();
                fileList.add(file);
                assignedFiles.put(id, fileList);
            }
        }
    }

    public void assignFile(String id, List<String> files) {
        for(String file : files) {
            assignFile(id, file);
        }
    }


    public void sendMapleMessage() {
//        for(final ProcessIdentifier pid : procIDs) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    sendMapleMessageToProc(pid, assignedFiles.get(pid.getId()));
//                }
//            }).start();
//        }
        for(ProcessIdentifier pid : procIDs) {
            Message mapleMessage =
                    MessagesFactory.generateMapleMessage(pid, procIDs,
                            cmdExe, filePrefix, assignedFiles.get(pid.getId()));

            MultiCast.broadCast(procIDs, mapleMessage);
        }
    }

    public void sendMapleMessageToProc(ProcessIdentifier pid, List<String> fileList) {
        Message mapleMessage =
                MessagesFactory.generateMapleMessage(proc.getIdentifier(), proc.getMemberList().getList(),cmdExe, filePrefix, fileList);
        TCPClient client = new TCPClient(pid);
        client.setProc(proc);
        if(client.connect()) {
            client.sendData(mapleMessage);
            client.close();
        }
    }

//    private void reassignFiles(String id) {
//        final List<String> filesNeedToReassign = assignedFiles.get(id);
//        assignedFiles.remove(id);
//        for(ProcessIdentifier pid: procIDs) {
//            if(pid.getId().equals(id)) {
//                procIDs.remove(pid);
//                break;
//            }
//        }
//
//        if(procIDs.size()>0) {
//            final ProcessIdentifier pid = procIDs.get(0);
//            assignFile(pid.getId(), filesNeedToReassign);
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    sendMapleMessageToProc(pid, filesNeedToReassign);
//                }
//            }).start();
//        } else {
//            System.out.println("No processes available, can't reassign tasks");
//        }
//    }

    public void monitor() {

    }

    public static void main(String args[]) {
        List<ProcessIdentifier> procIDs = new LinkedList<ProcessIdentifier>();
        for(int i=0; i<3;++i) {
            procIDs.add(
                    ProcessIdentifierFactory.generateProcessIdentifier(UUID.randomUUID().toString(), "1.1.1.1", 10000, 1));
        }

        MapleForMaster maple = new MapleForMaster();
        maple.procIDs = procIDs;

        List<String> fileList = new LinkedList<String>();
        fileList.add("1");
        fileList.add("2");
        fileList.add("3");
        fileList.add("4");
        fileList.add("5");
        fileList.add("6");
        fileList.add("7");
        fileList.add("8");
        fileList.add("9");
        fileList.add("10");

        maple.inputFiles = fileList;

        maple.assignFiles();
        System.out.println(maple.assignedFiles);
    }

    private Integer received = 0;
    public void onReceived() {
        synchronized (this) {
            received++;
            if(received == procIDs.size()) {
                sendDoMapleMessage();
            }
        }
    }

    private void sendDoMapleMessage() {
        for(final ProcessIdentifier pid : procIDs) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TCPClient tcpClient = new TCPClient(pid).setProc(proc);
                    if(tcpClient.connect()) {
                        tcpClient.sendData(MessagesFactory.generateDoMapleMessage(proc.getIdentifier()));
                    }
                }
            }).start();
        }
    }

}
