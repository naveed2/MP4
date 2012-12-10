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
                proc.getSDFS().createLocalSDFSFile(fileName);
//                createFile(fileName);
            }
            fileNames.add(fileName);
            values.add(result.getValue());

            if(fileNames.size() == 300) {
                cur+=300;
                System.gc();
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

    public void registerFailListener() {
        failureListener = new AbstractProcFailureListener(-1) {
            @Override
            public void run(ProcessIdentifier pid) {
                logger.info("Detecting failure, starts to reassign and rollback");
                rollback(pid.getId());
                reassignFiles(pid.getId());
            }
        };
        proc.getMemberList().registerFailureListener(failureListener);
    }

    public void unregisterFailListener() {
        proc.getMemberList().unregisterFailureListener(failureListener);
    }

    private void reassignFiles(String id) {
        final List<String> files = proc.getAndRemoveOtherMapleJobs(id);

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


    public void confirm(String id) {
        File rootDir = new File(proc.getSDFS().getRootDirectory());
        File[] files;
        if((files=rootDir.listFiles()) == null) {
            logger.error("wrong root dir");
            return;
        }
        for (File file : files) {
            String header = "_tmp_" + id + "_";
            if(file.getName().startsWith(header)) {
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
                } catch (IOException e) {
                    logger.error("read tmp file error", e);
                }

                if(!file.delete()) {
                    logger.error("fail to delete tmp file");
                }
            }
        }
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
