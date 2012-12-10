package membership;


import communication.*;
import filesystem.FileListScanning;
import filesystem.ReplicationManager;
import filesystem.SDFS;
import filesystem.UpdateManager;
import maplejuice.JuiceForMaster;
import maplejuice.MapleForClient;
import maplejuice.MapleForMaster;
import misc.MiscTool;
import misc.TimeMachine;
import org.apache.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.UUID;

import static communication.message.Messages.*;

public class Proc {
    private Integer tcpPort;
    private Integer udpPort;
    private Integer fileServerPort;

    private TCPServer tcpServer;
    private UDPServer udpServer;
    private TCPFileServer fileServer;

    private Boolean isTCPServerStarted;
    private Boolean isUDPServerStarted;
    private Boolean isFileServerStarted;

    private Gossip gossip;
    private FailureDetector failureDetector;
    private MemberListScanning memberListScanning;
    private FileListScanning fileListScanning;

    private SDFS SDFileSystem;

    private ReplicationManager replicationManager;
    private UpdateManager updateManager;

    private MapleForMaster mapleMaster;
    private MapleForClient mapleClient;

    private JuiceForMaster juiceMaster;

    private Logger logger = Logger.getLogger(Proc.class);
    private Integer timeStamp;
    private Integer localTime;
    private String id;
    private ProcessIdentifier identifier;
    private String hostAddress;
    private MemberList memberList;

    private ProcessIdentifier master;
    private Boolean isMaster;

    public Proc(Integer tcpPort) {
        this.timeStamp = 0;
        this.id = UUID.randomUUID().toString();
        this.tcpPort = tcpPort;
        this.udpPort = tcpPort + 1; // UDPPort is always set to TCPPort+1
        this.fileServerPort = tcpPort + 2;
        memberList = new MemberList();
        memberList.setProc(this);

        this.isMaster = false;
        this.master = null;

        this.isTCPServerStarted = this.isUDPServerStarted = this.isFileServerStarted = false ;
    }

    private void initIdentifier() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            hostAddress = addr.getHostAddress();
            identifier = ProcessIdentifierFactory.generateProcessIdentifier(id, hostAddress, tcpPort, timeStamp);
            System.out.println(identifier.toString());
        } catch (UnknownHostException e) {
            logger.fatal("Unknown local host", e);
            System.exit(-1);
        }

    }


    /**
     * initialize of whole system
     */
    public void init() {
        //init timeMachine
        TimeMachine.init();

        initIdentifier();
        addProcToMemberList(identifier);

        //TODO: before init new serve, old server should be closed.
        //init server
        initTCPServer();
        initUDPServer();
        initFileServer();

        //init gossip
        initGossip();

        //init failure detector
//        initFailureDetector();
//        initMemberListScanningThread();

        initSDFS();
        initFileListScan();

        initReplicaManger();
        initUpdateManager();

        initMultiCast();

        //TODO: this is for debug
        initTestFailureListeners();
    }

    private void initTestFailureListeners() {
        AbstractProcFailureListener testListener = new AbstractProcFailureListener(-1) {
            @Override
            public void run(ProcessIdentifier pid) {
                System.out.println("detecting a failure, this is test failure listeners");
            }
        };
        getMemberList().registerFailureListener(testListener);

        AbstractProcFailureListener testListener2 = new AbstractProcFailureListener(-1) {
            @Override
            public void run(ProcessIdentifier pid) {
                System.out.println("This sentence shouldn't appear in the screen");
            }
        };
        getMemberList().registerFailureListener(testListener2);
        getMemberList().unregisterFailureListener(testListener2);


    }

    public void stop() {
        tcpServer.stop();
        udpServer.stop();
        fileServer.stop();
        gossip.stop();
        replicationManager.stop();
        memberListScanning.stop();
        fileListScanning.stop();
    }

    private void initGossip() {
        gossip = new Gossip();
        gossip.setProc(this);

        gossip.start();
    }

    private void initFailureDetector() {
        failureDetector = new FailureDetector();
        failureDetector.setProc(this);

        failureDetector.start();
    }

    private void initMemberListScanningThread() {
        memberListScanning = new MemberListScanning();
        memberListScanning.setProc(this);
        memberListScanning.startScan();
    }

    private void initFileListScan() {
        fileListScanning = new FileListScanning();
        fileListScanning.setProc(this);
//        fileListScanning.startScan();
    }

    private void initReplicaManger(){
        replicationManager = new ReplicationManager();
        replicationManager.setProc(this);
//        replicationManager.start();
    }

    private void initUpdateManager() {
        updateManager = new UpdateManager();
        updateManager.setProc(this);
//        updateManager.start();
    }

    public void initTCPServer() {
        tcpServer = new TCPServer(tcpPort);
        tcpServer.setProc(this);

        if(tcpServer.start()) {
            isTCPServerStarted = true;
            logger.info("TCP Server starts successfully, listening to port " + tcpPort);
        } else {
            System.err.println("TCP Server starts failed, please check configuration");
            logger.fatal("TCP Server starts failed");
        }
    }

    public void initUDPServer() {
        udpServer = new UDPServer(udpPort);
        udpServer.setProc(this);

        if(udpServer.start()) {
            isUDPServerStarted = true;
            logger.info("UDP Server starts successfully, listening to port " + udpPort);
        } else {
            System.err.println("UDP Server starts failed, please check configuration");
            logger.fatal("UDP Server starts failed");
        }
    }

    private void initFileServer() {
        fileServer = new TCPFileServer(fileServerPort);
        fileServer.setProc(this);

        if(fileServer.start()) {
            isFileServerStarted = true;
            logger.info("TCP File Server starts successfully, listening to port " + fileServerPort);
        } else {
            System.err.println("TCP File Server starts failed, please check configuration");
            logger.fatal("TCP Server starts failed");
        }
    }

    private void initSDFS() {
        String rootDir = MiscTool.inputSDFSRoot(new Scanner(System.in));
        if(!rootDir.endsWith("/")) {
            rootDir += "/";
        }
        SDFileSystem = new SDFS(rootDir);
        SDFileSystem.setProc(this);
        SDFileSystem.init();
    }

    private void initMultiCast() {
        MultiCast.init(this);
    }

    public ProcessIdentifier getIdentifier() {
        return ProcessIdentifierFactory.generateProcessIdentifier(id, hostAddress, tcpPort, timeStamp);
    }

    public String getId() {
        return id;
    }

    public MemberList getMemberList() {
        return memberList;
    }

    public void addProcToMemberList(ProcessIdentifier processIdentifier) {
        memberList.add(processIdentifier);
    }

    public Integer getTimeStamp() {
        synchronized (this) {
            return timeStamp;
        }
    }

    public Integer increaseAndGetTimeStamp() {
        synchronized (this) {
            return ++timeStamp;
        }
    }

    public Integer getTcpPort() {
        return tcpPort;
    }

    public FailureDetector getFailureDetector() {
        return failureDetector;
    }

    public Proc setMemberList(MemberList memberList) {
        this.memberList = memberList;
        return this;
    }

    public TCPFileServer getFileServer() {
        return fileServer;
    }

    public SDFS getSDFS() {
        return SDFileSystem;
    }

    public ReplicationManager getReplicaManger() {
        return replicationManager;
    }

    public Proc setMaster(ProcessIdentifier masterProcess) {
        if(masterProcess.getId().equals(getId())) {
            isMaster = true;
        } else {
            isMaster = false;
            master = masterProcess;
        }
        return this;
    }

    public Proc setMapleMaster(MapleForMaster mapleMaster) {
        this.mapleMaster = mapleMaster;
        return this;
    }

    public Proc setMapleClient(MapleForClient mapleClient) {
        this.mapleClient = mapleClient;
        return this;
    }

    public MapleForMaster getMapleMaster() {
        return mapleMaster;
    }

    public MapleForClient getMapleClient() {
        return mapleClient;
    }

}
