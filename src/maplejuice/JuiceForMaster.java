package maplejuice;

import communication.message.Messages;
import membership.PIDComparator;
import membership.Proc;

import java.util.*;

import static communication.message.Messages.FileIdentifier;
import static communication.message.Messages.ProcessIdentifier;

public class JuiceForMaster {

    private Proc proc;

    private String cmdExe;
    private Integer numJuice;
    private String prefix;
    private String destFileName;

    private Map<Integer, String> assignedJuices;
    private Map<Integer, List<FileIdentifier>> filesAssignedToJuice;

    public JuiceForMaster() {
        assignedJuices = new HashMap<Integer, String>();
        filesAssignedToJuice = new HashMap<Integer, List<FileIdentifier>>();
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

    }

    private void assignFilesToJuices(Integer numJuice, String prefix) {
        List<FileIdentifier> fidList = getInputFiles(proc, prefix);

        List<Messages.ProcessIdentifier> pidList =
                new LinkedList<Messages.ProcessIdentifier>(proc.getMemberList().getList());
        Collections.sort(pidList, new PIDComparator());
        pidList = pidList.subList(0, numJuice);

        Iterator<Messages.ProcessIdentifier> pidIterator = pidList.iterator();

        int count=0;
        for(FileIdentifier fid : fidList) {
            if(filesAssignedToJuice.containsKey(count)) {
                List<FileIdentifier> tmpList = filesAssignedToJuice.get(count);
                tmpList.add(fid);
            } else {
                List<FileIdentifier> newList = new LinkedList<FileIdentifier>();
                newList.add(fid);
                filesAssignedToJuice.put(count, newList);
            }

            count = (count +1) % numJuice;
        }

        count = 0;
        for(ProcessIdentifier pid : pidList) {
            assignedJuices.put(count, pid.getId());
            ++count;
            if(count == numJuice) {
                break;
            }
        }

    }

    private List<FileIdentifier> getInputFiles(Proc proc, String prefix) {
        List<FileIdentifier> res = new LinkedList<FileIdentifier>();
        for(FileIdentifier fid : proc.getSDFS().getFileList()) {
            if(fid.getFileName().startsWith(prefix)) {
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
