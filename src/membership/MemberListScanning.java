package membership;

import java.util.concurrent.atomic.AtomicBoolean;

public class MemberListScanning {

    private Proc proc;
    private AtomicBoolean shouldStop;
    private static final Integer INTERVAL = 2000;

    public MemberListScanning() {
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
                    if(proc.getMemberList().updateMemberList()) {
                        System.out.println("Detecting machine failed, start replication");
                        proc.getReplicaManger().interruptSleep();
                    }
                }
            }
        }).start();
    }

    public void stop() {
        shouldStop.set(true);
    }
}
