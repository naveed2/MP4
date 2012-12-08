package membership;

import communication.message.Messages;

import static communication.message.Messages.ProcessIdentifier;

public interface IProcFailureListener {

    public void apply(ProcessIdentifier pid);

    public boolean canRemove();
}
