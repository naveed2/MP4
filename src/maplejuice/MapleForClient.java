package maplejuice;

import communication.MultiCast;
import communication.TCPClient;
import communication.message.Messages;
import communication.message.MessagesFactory;
import membership.Proc;
import misc.MiscTool;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import static communication.message.Messages.MapleMessage;
import static communication.message.Messages.Message;
import static communication.message.Messages.ProcessIdentifier;

public class MapleForClient {

    private static Logger logger = Logger.getLogger(MapleForClient.class);

    private Proc proc;

    private MapleMessage mapleMessage;
    private ProcessIdentifier master;
    private String cmdExe;
    private String preFix;
    private List<String> inputFileList;
    private List<ProcessIdentifier> pidList;


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
        master = mapleMessage.getFromMachine();

        System.out.println("Received maple command: " + cmdExe + ", " + preFix + ", " + inputFileList);

        for(String file: inputFileList) {
            if(!proc.getSDFS().isLocalFile(file)) {
                proc.getSDFS().getRemoteFile(file, proc.getSDFS().getRootDirectory() + file);
                proc.getSDFS().loadFileFromRootDirectory(new File(file));
            }
        }

        sendReceivedMapleMessage(master);
    }

    private void sendReceivedMapleMessage(ProcessIdentifier master) {
        TCPClient tcpClient = new TCPClient(master).setProc(proc);
        if(tcpClient.connect()) {
            tcpClient.sendData(MessagesFactory.generateReceivedMapleMessage(proc.getIdentifier()));
            tcpClient.close();
        }
    }

    public void doMaple() {
        final List<String> command = new LinkedList<String>();
        command.add("./" + cmdExe);
        for(String file: inputFileList) {
//            if(!proc.getSDFS().isLocalFile(file)) {
//                proc.getSDFS().getRemoteFile(file, proc.getSDFS().getRootDirectory() + file);
//                proc.getSDFS().loadFileFromRootDirectory(new File(file));
//            }
            command.add(proc.getSDFS().getRootDirectory() + file);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Run maple command: " + command);
                runCommand(command);
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
        for(Map.Entry<String, String> result : mapleResult.entrySet()) {
            String key = result.getKey();
            String fileName = preFix + "_" + key;
            if(MiscTool.requireToCreateFile(pidList, proc.getIdentifier(), fileName)) {
                proc.getSDFS().createLocalSDFSFile(fileName);
//                createFile(fileName);
            }

            sendResult(fileName, result.getValue());
            ++cur;
            System.out.println("Progress: " + cur + "/" + size);
        }
    }

    public void sendResult(String fileName, String value) {
        Message mapleResult = MessagesFactory.generateMapleResultMessage(proc.getIdentifier(), fileName, value);
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


    public static void main(String[] args) throws IOException {
        List<String> cmds = new LinkedList<String>();
        cmds.add("./maple_exe");
        cmds.add("input1");
        cmds.add("input2");
        cmds.add("input3");


        List<String> fileList = new LinkedList<String>();
        fileList.add("input1");
        fileList.add("input2");
        fileList.add("input3");
        MapleForClient maple = new MapleForClient();
        maple.cmdExe="maple_exe";
        maple.inputFileList = fileList;
        maple.mapleResult = new HashMap<String, String>();
        maple.doMaple();



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
