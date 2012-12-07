package main;

import communication.message.Messages;
import communication.message.MessagesFactory;
import communication.TCPClient;
import filesystem.FileState;
import membership.Proc;
import membership.ProcState;
import misc.MiscTool;
import misc.TimeMachine;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Scanner;

import static communication.message.Messages.*;

public class MainEntry {

    private static Scanner in  = new Scanner(System.in);
    private static CommandMap commandMap = CommandMap.getInstance();
    private static Proc proc;
    private static Integer localPort;

    private static Logger logger = Logger.getLogger(MainEntry.class);

    public static void main(String[] args) {

        log4jConfigure();
        init();
        work();

    }

    /**
     * Configuration for log4j
     */
    private static void log4jConfigure() {
        PropertyConfigurator.configure("log4j.properties");
        System.out.println("configure log4j successfully");
    }

    /**
     * System command initalzied
     */

    private static void init() {
        commandMap.initialize();

        printWelcomeMessage();
    }


    private static String arg1, arg2;
    /**
     * the main function
     */
    public static void work() {

        while(true) {
            String inputLine = inputCommand();
            if(inputLine.length() ==0) {
                continue;
            }

            String[] arr = inputLine.split(" ");
            String cmd = arr[0];
            if(arr.length >=2) {
                arg1 = arr[1];
            }
            if(arr.length >=3) {
                arg2 = arr[2];
            }

            String funcName = commandMap.findCommand(cmd);

            if(funcName == null) {
                //TODO: wrong command
            } else if(funcName.equals("quit")) {
                proc.stop();
                break;
            } else if(funcName.equals("printHelp")) {
                CommandMap.printHelp();
            } else {
                MiscTool.callStaticMethod(MainEntry.class, funcName);
            }
        }

        System.out.println("Program quits");
    }

    /**
     * start the server
     */
    private static void start() {
        localPort = MiscTool.inputPortNumber(in);
        proc = new Proc(localPort);
        proc.init();
    }

    /**
     * command of join
     */
    private static void joinGroup() {
        String address = MiscTool.inputAddress(in);
        System.out.println("Start connecting to " + address);
        TCPClient tcpClient = new TCPClient(address);
        tcpClient.setProc(proc);
        if(tcpClient.connect()) {
            Message m = MessagesFactory.generateJoinMessage(
                    proc.getId(), proc.getIdentifier().getIP(), localPort, proc.increaseAndGetTimeStamp());
            tcpClient.sendData(m);
            tcpClient.close();
        }
    }

    /**
     * command of show member list
     */
    private static void showMemberList() {
        int pos = 0;
        for(Messages.ProcessIdentifier identifier : proc.getMemberList()) {
            Integer timeStamp;
            String address;
            Long localTime;
            ProcState procState;

            if(isMySelf(identifier)) {
                timeStamp = proc.getTimeStamp();
                address = "127.0.0.1:" + proc.getTcpPort();
                localTime = TimeMachine.getTime();
                procState = ProcState.available;
            } else {
                timeStamp = identifier.getTimestamp();
                address = identifier.getIP() + ":" +identifier.getPort();
                localTime = proc.getMemberList().getTime(identifier);
                procState = proc.getMemberList().getState(identifier);
            }
            System.out.println(
                    identifier.getId() + '\t' + address + '\t' + timeStamp + '\t' + localTime + '\t' + procState);
            ++pos;
        }
    }

    /**
     * command of show file list
     */
    private static void showFileList() {
        for(FileIdentifier fileIdentifier : proc.getSDFS().getFileList()) {
            ProcessIdentifier identifier = fileIdentifier.getFileStoringProcess();
            if(!proc.getSDFS().isValid(fileIdentifier)) {
                continue;
            }
            String address;
            Integer timeStamp;
            Long localTime;
            FileState fileState;

            if(isMySelf(identifier)) {
                address = "127.0.0.1:" + proc.getTcpPort();
                timeStamp = proc.getTimeStamp();
//                localTime = TimeMachine.getTime();
                localTime = proc.getSDFS().getFileLocalTime(fileIdentifier);
            } else {
                address = identifier.getIP()+":"+identifier.getPort();
                timeStamp = proc.getSDFS().getFileTimeStamp(fileIdentifier);
                localTime = proc.getSDFS().getFileLocalTime(fileIdentifier);
            }
            fileState = proc.getSDFS().getFileState(fileIdentifier);
            System.out.println(
                    fileIdentifier.getFileName() + '\t' + address + '\t' + timeStamp + '\t' + localTime + '\t' +fileState);
        }
    }

    /**
     * command of put file
     */

    private static void putFile() {
//        String fileName = MiscTool.inputFileName(in);
        long startTime = System.currentTimeMillis();
        proc.getSDFS().addFileLocally(arg1, arg2);
        long usingTime = System.currentTimeMillis() - startTime;

        logger.info("put command uses " + usingTime + " ms");
    }

    /**
     * command of delete file
     */
    private static void deleteFile() {
        proc.getSDFS().deleteFile(arg1,true);
    }

    /**
     * command of get file
     */

    private static void getFile(){
//        String remoteFileName = MiscTool.inputFileName(in);
//        String localFileName = MiscTool.inputFileName(in);
        String remoteFileName = arg1;
        String localFileName = arg2;
        long startTime = System.currentTimeMillis();
        proc.getSDFS().getRemoteFile(remoteFileName, localFileName);
        long usingTime = System.currentTimeMillis() - startTime;

        logger.info("Get command uses " + usingTime + " ms");
    }

    private static boolean isMySelf(ProcessIdentifier identifier) {
        return identifier.getId().equals(proc.getId());
    }

    private static String inputCommand() {
        System.out.print(">");
        return in.nextLine();
    }

    private static void printWelcomeMessage() {
        System.out.println("Welcome to the fictitious Group-R-Us Inc.!");
        System.out.println("Author: Muhammad Naveed, Junjie Hu");
    }

    public Proc getProc() {
        return proc;
    }
}
