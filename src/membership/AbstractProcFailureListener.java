package membership;

import static communication.message.Messages.*;

public abstract class AbstractProcFailureListener implements IProcFailureListener {

    private Integer count;

    /**
     * Count is listener running time. -1 for infinite.
     * @param count running time
     */
    public AbstractProcFailureListener(Integer count) {
        this.count = count;
    }

    @Override
    public void apply(ProcessIdentifier pid) {
        if(count ==0) {
            return;
        }

        if(count>0) {
            count -=1;
        }
        run(pid);
    }

    @Override
    public boolean canRemove() {
        return count == 0;
    }

    public abstract void run(ProcessIdentifier pid);
}
