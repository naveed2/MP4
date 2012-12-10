package maplejuice;

import communication.MultiCast;
import communication.TCPClient;
import communication.message.MessagesFactory;
import membership.AbstractProcFailureListener;
import membership.Proc;
import misc.MiscTool;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import static communication.message.Messages.MapleMessage;
import static communication.message.Messages.Message;
import static communication.message.Messages.ProcessIdentifier;

/**
 * This maple for client part.
 */
public class MapleForClient {

    private static Logger logger = Logger.getLogger(MapleForClient.class);

    private Proc proc;

    private MapleMessage mapleMessage;
    private String cmdExe;
    private String preFix;
    private List<String> inputFileList;
    private List<ProcessIdentifier> pidList;

    private AbstractProcFailureListener failureListener;

    private boolean hasReceivedDoMaple = false;

    private Set<String> confirmPid = new HashSet<String>();


    private Map<String, String> mapleResult;

    public MapleForClient() {
    }

    public MapleForClient setMapleMessage(MapleMessage mapleMessage) {
        this.mapleMessage = mapleMessage;
        return this;
    }

    public void init() {
        mapleResult = new HashMap<String, String>();
        cmdExe = mapleMessage.getCmdExe();
        preFix = mapleMessage.getPrefix();
        inputFileList = mapleMessage.getFileListList();
        pidList = mapleMessage.getMachinesList();

        System.out.println("Received maple command: " + cmdExe + ", " + preFix + ", " + inputFileList);

        for(String file: inputFileList) {
            if(!proc.getSDFS().isLocalFile(file)) {
                proc.getSDFS().getRemoteFile(file, proc.getSDFS().getRootDirectory() + file);
                proc.getSDFS().loadFileFromRootDirectory(new File(file));
            }
        }

        sendReceivedMapleMessage(proc.getMaster());
    }

    private void sendReceivedMapleMessage(ProcessIdentifier master) {
        TCPClient tcpClient = new TCPClient(master).setProc(proc);
        if(tcpClient.connect()) {
            tcpClient.sendData(MessagesFactory.generateReceivedMapleMessage(proc.getIdentifier()));
            tcpClient.close();
        }
    }

    public void doMaple() {

        hasReceivedDoMaple = true;
        System.out.println("maple start time: " + System.currentTimeMillis());

        registerFailListener();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(String file: inputFileList) {
                    final List<String> command = new LinkedList<String>();
                    command.add("./" + cmdExe);
                    command.add(proc.getSDFS().getRootDirectory() + file);
                    logger.info("Run maple command: " + command);
                    runCommand(command);
                    mapleResult.clear();
                }
//                unregisterFailListener();
                MultiCast.broadCast(pidList, MessagesFactory.generateMapleFinishMessage(proc.getIdentifier()));
                System.out.println("maple end time: " + System.currentTimeMillis());
            }
        }).start();
    }

    public void runCommand(List<String> cmd) {

        ProcessBuilder pb = new ProcessBuilder(cmd);
        try {
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new BufferedInputStream(p.getInputStream())));

            String strLine;
            while((strLine = reader.readLine()) != null) {
//                String[] pair=strLine.split(",");
                String[] pair = new String[2];
                int commaPos = strLine.indexOf(",");
                pair[0] = strLine.substring(0, commaPos);
                //TODO: need to solve special characters right here
                mapleResult.put(pair[0], strLine);
//                System.out.println(pair[0]+ ", " + pair[1]);
            }

//            System.out.println(mapleResult);
            saveResults();

        } catch (IOException e) {
            logger.error("Maple job error "+ e);
        }
    }

    public void saveResults() {
        Integer size = mapleResult.entrySet().size();
        Integer cur = 0;
        List<String> fileNames = new LinkedList<String>();
        List<String> values = new LinkedList<String>();

        for(Map.Entry<String, String> result : mapleResult.entrySet()) {
            String key = result.getKey();
            String fileName = preFix + "_" + key;
            if(MiscTool.requireToCreateFile(pidList, proc.getIdentifier(), fileName)) {
//                proc.getSDFS().createLocalSDFSFile(fileName);
//                createFile(fileName);
            }
            fileNames.add(fileName);
            values.add(result.getValue());

            if(fileNames.size() == 800) {
                cur+=800;
                sendResult(fileNames, values);
                fileNames.clear();
                values.clear();
                System.out.println("Progress: " + cur + "/" + size);
            }

        }

        if(fileNames.size() != 0) {
            cur += fileNames.size();
            sendResult(fileNames, values);
            System.out.println("Progress: " + cur + "/" + size);
        }
    }

    public void sendResult(List<String> fileNames, List<String> values) {
        Message mapleResult = MessagesFactory.generateMapleResultMessage(proc.getIdentifier(), fileNames, values);
        MultiCast.broadCast(proc.getMemberList().getList(), mapleResult);
    }


    private void createFile(String fileName) {
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public MapleForClient setProc(Proc proc) {
        this.proc = proc;
        return this;
    }

    public List<ProcessIdentifier> getPidList() {
        return pidList;
    }

    private static boolean registered = false;

    public void registerFailListener() {
        if(!MapleForClient.registered) {
            failureListener = new AbstractProcFailureListener(-1) {
                @Override
                public void run(ProcessIdentifier pid) {
                    if(!confirmPid.contains(pid.getId())) {
                        logger.info("Detecting failure, starts to reassign and rollback");
                        rollback(pid.getId());
                        reassignFiles(pid.getId());
                    }
                }
            };
            proc.getMemberList().registerFailureListener(failureListener);
            MapleForClient.registered = true;
        }
    }

    public void unregisterFailListener() {
        proc.getMemberList().unregisterFailureListener(failureListener);
    }

    private void reassignFiles(String id) {
        final List<String> files = proc.getAndRemoveOtherMapleJobs(id);
        if(files.size() == 0) return;

        int pos=-1;
        for(int i=0; i<pidList.size();++i) {
            if(pidList.get(i).getId().equals(id)) {
                pos = i;
                break;
            }
        }

        if(pos == -1){
            logger.error("Something wrong with reassign files");
            return;
        }

        ProcessIdentifier pid;

        if(pos == pidList.size() -1) {
            pid = pidList.get(0);
        } else {
            pid = pidList.get(pos+1);
        }

        if(!pid.getId().equals(proc.getId())) {
            return;
        }

        for(String file: files) {
            if(!proc.getSDFS().isLocalFile(file)) {
                proc.getSDFS().getRemoteFile(file, proc.getSDFS().getRootDirectory() + file);
                proc.getSDFS().loadFileFromRootDirectory(new File(file));
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                for(String file: files) {
                    final List<String> command = new LinkedList<String>();
                    command.add("./" + cmdExe);
                    command.add(proc.getSDFS().getRootDirectory() + file);
                    logger.info("Run maple command: " + command);
                    runCommand(command);
                    mapleResult.clear();
                }
//                unregisterFailListener();
                MultiCast.broadCast(pidList, MessagesFactory.generateMapleFinishMessage(proc.getIdentifier()));
                System.out.println("maple end time: " + System.currentTimeMillis());
            }
        }).start();

        if(pid.getId().equals(proc.getId())) {
            Message mapleMessage = MessagesFactory.generateMapleMessage(proc.getIdentifier(), pidList, cmdExe, preFix, files);
            TCPClient tcpClient = new TCPClient(proc.getIdentifier()).setProc(proc);
            if(tcpClient.connect()) {
                tcpClient.sendData(mapleMessage);
                tcpClient.close();
            } else {
                logger.error("Reassign task fail");
            }
        }
    }

    private void rollback(String id) {
        File rootDir = new File(proc.getSDFS().getRootDirectory());
        File[] files;
        if((files=rootDir.listFiles()) == null) {
            logger.error("wrong root dir");
            return;
        }
        for (File file : files) {
            if(file.getName().startsWith("_tmp_" + id)) {
                if(!file.delete()) {
                    logger.error("fail to delete tmp file");
                }
            }
        }
    }

    private boolean isConfirming = false;
    public void confirm(String id) {
        System.out.println(MiscTool.getDate() + ":" + "Start committing...");
        confirmPid.add(id);

        if(!isConfirming) {
            isConfirming = true;
        } else {
            return;
        }
        File rootDir = new File(proc.getSDFS().getRootDirectory());
        File[] files;
        boolean flag = true;
        while(true) {
            flag = true;
            if((files=rootDir.listFiles()) == null) {
                logger.error("wrong root dir");
                return;
            }
            for (File file : files) {
                String header;
                Integer prefixPos = file.getName().indexOf(preFix);
                if(prefixPos == -1) continue;

                header = file.getName().substring(0, prefixPos);
                if(!(header.startsWith("_tmp_") || !header.endsWith("_"))) {
                    continue;
                }
                if(header.length() ==0) {
                    continue;
                }
                String idInHeader;

                try {
                    idInHeader = header.substring(5, header.length()-1);
                } catch (StringIndexOutOfBoundsException e) {
                    logger.info("string index out of bounds: " + header, e);
                    continue;
                }

//                if(file.getName().startsWith(header)) {
                if(confirmPid.contains(idInHeader)) {
                    String newFileName = file.getName().substring(header.length());
                    if(!proc.getSDFS().hasSDFSFile(newFileName)) {
                        proc.getSDFS().createLocalSDFSFile(newFileName);
                    }
                    flag = false;
                    BufferedReader br;
                    try {
                        br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    } catch (FileNotFoundException e) {
                        logger.error("file not found", e);
                        continue;
                    }
                    String data;
                    try {
                        while((data = br.readLine())!=null ) {
                            proc.getSDFS().appendDataToLocalFile(file.getName().substring(header.length()), data);
                        }
                        br.close();
                    } catch (IOException e) {
                        logger.error("read tmp file error", e);
                    }

                    if(!file.delete()) {
                        logger.error("fail to delete tmp file");
                    }
                }
            }

            if(flag) {
                break;
            }
        }
        System.out.println(MiscTool.getDate() + ":" + "Committing done");
    }

    public static void main(String[] args) throws IOException {
        System.gc();


//        ProcessBuilder pb = new ProcessBuilder(cmds);
//
//        Process p = pb.start();
//        BufferedInputStream bis = new BufferedInputStream(p.getInputStream());
//        BufferedReader br = new BufferedReader(new InputStreamReader(bis));
//
//        String lineStr;
//        while((lineStr=br.readLine()) != null) {
//            System.out.println(lineStr);
//        }
    }
}
