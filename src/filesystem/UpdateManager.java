package filesystem;

import communication.message.Messages;
import membership.Proc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static communication.message.Messages.FileIdentifier;

public class UpdateManager {
    private Proc proc;
    private AtomicBoolean shouldStop;

    private Integer scanInterval = 15000;
    private Thread scanThread;


    public UpdateManager setProc(Proc proc) {
        this.proc = proc;
        shouldStop = new AtomicBoolean(false);
        return this;
    }

    public void start() {
        scanThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!shouldStop.get()) {
                    try {
                        Thread.sleep(scanInterval);
                    } catch (InterruptedException e) {
                        //
                    }
                    scanList();
                }
            }
        });

        scanThread.start();
    }

    private void scanList() {
        FileList fileList = proc.getSDFS().getFileList();
        Set<FileIdentifier> localFIds = new HashSet<FileIdentifier>();
        Map<String, FileIdentifier> remoteFileMap = new HashMap<String, FileIdentifier>();

        for(FileIdentifier fid : fileList) {
            if(!proc.getSDFS().isAvailable(fid)) {
                continue;
            }
            if(fid.getFileStoringProcess().getId().equals(proc.getId())) {
                localFIds.add(fid);
            } else {
                String fileName = fid.getFileName();
                if(remoteFileMap.containsKey(fileName)) {
                    Long lastTime = remoteFileMap.get(fileName).getLastWriteTime();
                    if(fid.getLastWriteTime() > lastTime) {
                        remoteFileMap.put(fid.getFileName(), fid);
                    }
                } else {
                    remoteFileMap.put(fid.getFileName(), fid);
                }
            }
        }

        for(FileIdentifier fid: localFIds) {
            String fileName = fid.getFileName();
            if(remoteFileMap.containsKey(fileName)) {
                FileIdentifier remoteFid = remoteFileMap.get(fileName);
                if(remoteFid.getLastWriteTime() < fid.getLastWriteTime()) {
                    new FileOperations().setProc(proc).sendPutMessage(
                            fid, remoteFid.getFileStoringProcess().getIP(), remoteFid.getFileStoringProcess().getPort());
                }
            }
        }
    }

    public void stop() {
        shouldStop.set(true);
    }


}
