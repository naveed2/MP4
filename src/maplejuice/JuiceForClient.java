package maplejuice;


import communication.MultiCast;
import communication.message.MessagesFactory;
import membership.Proc;
import misc.MiscTool;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static communication.message.Messages.JuiceMessage;
import static communication.message.Messages.Message;

public class JuiceForClient {
    private static Logger logger = Logger.getLogger(JuiceForClient.class);

    private Proc proc;

    private JuiceMessage juiceMessage;
    private String cmdExe;
    private String destFileName;
    private Integer numJuice;
    private List<String> inputFileList;

    private Map<String, String> juiceResult;

    public JuiceForClient() {
        juiceResult = new HashMap<String, String>();
    }

    public JuiceForClient setJuiceMessage(JuiceMessage juiceMessage) {
        this.juiceMessage = juiceMessage;
        return this;
    }

    public JuiceForClient init() {
        cmdExe = juiceMessage.getCmdExe();
        destFileName = juiceMessage.getDestFileName();
        inputFileList = juiceMessage.getFileListList();
        numJuice = juiceMessage.getNumJuice();
        return this;
    }

    public void doJuice() {
        final List<String> command = new LinkedList<String>();
        command.add("./" + cmdExe);
        for(String file: inputFileList) {
            if(!proc.getSDFS().isLocalFile(file)) {
                proc.getSDFS().getRemoteFile(file, proc.getSDFS().getRootDirectory() + file);
                proc.getSDFS().loadFileFromRootDirectory(new File(file));
            }
            command.add(proc.getSDFS().getRootDirectory() + file);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                logger.info("Run juice command: " + command);
                runCommand(command);
            }
        }).start();
    }

    public void runCommand(List<String> cmd) {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        try {
            Process p = pb.start();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new BufferedInputStream(p.getInputStream())));

            String strLine;
            while((strLine = br.readLine()) != null) {
                String[] pair = new String[2];
                int commaPos = strLine.indexOf(",");
                pair[0] = strLine.substring(0, commaPos);
                pair[1] = strLine.substring(commaPos+1, strLine.length());
                juiceResult.put(pair[0], pair[1]);
            }

            saveResults();
        } catch(IOException e) {
            logger.error("Maple job error "+ e);
        }
    }

    private void saveResults() {
        for(Map.Entry<String, String> result : juiceResult.entrySet()) {

            if(MiscTool.requireToCreateFile(proc.getMemberList().getList(), proc.getIdentifier(), destFileName, numJuice)) {
                proc.getSDFS().createLocalSDFSFile(destFileName);
            }

            sendResult(destFileName, result.getKey(), result.getValue());
        }
    }

    public void sendResult(String fileName, String key, String value) {
        Message juiceResult = MessagesFactory.generateJuiceResultMessage(
                proc.getIdentifier(),fileName,key,value, numJuice);
        MultiCast.broadCast(proc.getMemberList().getList(), juiceResult);
    }

    public JuiceForClient setProc(Proc proc) {
        this.proc = proc;
        return this;
    }
}
