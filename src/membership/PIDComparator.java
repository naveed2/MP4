package membership;

import communication.message.Messages;

import java.util.Comparator;

public class PIDComparator implements Comparator<Messages.ProcessIdentifier>{

    @Override
    public int compare(Messages.ProcessIdentifier pid1, Messages.ProcessIdentifier pid2) {
        int hash1 = pid1.getId().hashCode();
        int hash2 = pid2.getId().hashCode();
        if(hash1 == hash2) {
            return 0;
        } else if(hash1 < hash2){
            return -1;
        } else {
            return 1;
        }
    }
}
