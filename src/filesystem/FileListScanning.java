package filesystem;

import membership.Proc;

import java.util.concurrent.atomic.AtomicBoolean;

public class FileListScanning {
    private Proc proc;
    private AtomicBoolean shouldStop;
    private static final Integer INTERVAL = 1000;

    public FileListScanning() {
        shouldStop = new AtomicBoolean(false);
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }

    public void startScan() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!shouldStop.get()) {
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        //
                    }
                    proc.getSDFS().updateFileList();
                }
            }
        }).start();
    }

    public void stop() {
        shouldStop.set(true);
    }
}
