package maplejuice;

import communication.TCPClient;
import communication.message.Messages;
import communication.message.MessagesFactory;
import membership.PIDComparator;
import membership.Proc;

import java.util.*;

import static communication.message.Messages.FileIdentifier;
import static communication.message.Messages.Message;
import static communication.message.Messages.ProcessIdentifier;

/**
 * This is juice for master. It's usage is to assign intermediate files to procs.
 */
public class JuiceForMaster {

    private Proc proc;

    private String cmdExe;
    private Integer numJuice;
    private String prefix;
    private String destFileName;

    private Map<Integer, ProcessIdentifier> assignedJuices;
    private Map<Integer, List<String>> filesAssignedToJuice;

    public JuiceForMaster() {
        assignedJuices = new HashMap<Integer, ProcessIdentifier>();
        filesAssignedToJuice = new HashMap<Integer, List<String>>();
    }

    public JuiceForMaster setProc(Proc proc) {
        this.proc = proc;
        return this;
    }

    public void run(String cmdExe, Integer number, String prefix, String destFileName) {
        this.cmdExe = cmdExe;
        this.numJuice = number;
        this.prefix = prefix;
        this.destFileName = destFileName;

        checkJuiceNumber();

        assignFilesToJuices(this.numJuice, this.prefix);

        registerFailListener();

        sendJuiceMessage();
    }

    private void sendJuiceMessage() {
        for(final Map.Entry<Integer, ProcessIdentifier> entry : assignedJuices.entrySet()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Integer numJuice = entry.getKey();
                    sendJuiceMessageToProc(assignedJuices.get(numJuice), filesAssignedToJuice.get(numJuice));
                }
            }).start();
        }
    }

    private void sendJuiceMessageToProc(ProcessIdentifier pid, List<String> fileList) {
        while(fileList.size()>0) {
            Integer subSize = Math.min(fileList.size(), 1000);
            List<String> subList = fileList.subList(0, subSize);
            fileList = fileList.subList(subSize, fileList.size());

            Message juiceMessage = MessagesFactory.generateJuiceMessage(
                    proc.getIdentifier(), cmdExe, destFileName, numJuice, subList);
            TCPClient tcpClient = new TCPClient(pid).setProc(proc);
            if(tcpClient.connect()) {
                tcpClient.sendData(juiceMessage);
                tcpClient.close();
            }
        }
    }

    private void registerFailListener() {

    }

    private void assignFilesToJuices(Integer numJuice, String prefix) {
        List<FileIdentifier> fidList = getInputFiles(proc, prefix);

        List<Messages.ProcessIdentifier> pidList =
                new LinkedList<Messages.ProcessIdentifier>(proc.getMemberList().getList());
        Collections.sort(pidList, new PIDComparator());
        pidList = pidList.subList(0, numJuice);

//        int count=0;
//        for(FileIdentifier fid : fidList) {
//            if(filesAssignedToJuice.containsKey(count)) {
//                List<String> tmpList = filesAssignedToJuice.get(count);
//                tmpList.add(fid.getFileName());
//            } else {
//                List<String> newList = new LinkedList<String>();
//                newList.add(fid.getFileName());
//                filesAssignedToJuice.put(count, newList);
//            }
//
//            count = (count +1) % numJuice;
//        }

        for(FileIdentifier fid : fidList) {
            Integer hashCode = fid.getFileName().hashCode();
            Integer pos = hashCode % numJuice;
            if(filesAssignedToJuice.containsKey(pos)) {
                List<String> tmpList = filesAssignedToJuice.get(pos);
                tmpList.add(fid.getFileName());
            } else {
                List<String> newList = new LinkedList<String>();
                newList.add(fid.getFileName());
                filesAssignedToJuice.put(pos, newList);
            }
        }

        int count = 0;
        for(ProcessIdentifier pid : pidList) {
            assignedJuices.put(count, pid);
            ++count;
            if(count == numJuice) {
                break;
            }
        }

//        count = 0;
//        for(ProcessIdentifier pid : pidList) {
//            assignedJuices.put(count, pid);
//            ++count;
//            if(count == numJuice) {
//                break;
//            }
//        }

    }

    private List<FileIdentifier> getInputFiles(Proc proc, String prefix) {
        Set<String> fileNames = new HashSet<String>();
        List<FileIdentifier> res = new LinkedList<FileIdentifier>();
        for(FileIdentifier fid : proc.getSDFS().getFileList().getList()) {
            if(fid.getFileName().startsWith(prefix + "_") && !fileNames.contains(fid.getFileName())) {
                fileNames.add(fid.getFileName());
                res.add(fid);
            }
        }
        return res;
    }

    private void checkJuiceNumber() {
        if(numJuice > proc.getMemberList().size()) {
            System.out.println("#Juice exceeds #procs, reduce it to #procs");
            this.numJuice = proc.getMemberList().size();
        }
    }
}
