package membership;

import communication.message.Messages.ProcessIdentifier;
import misc.TimeMachine;
import org.apache.log4j.Logger;

import java.util.*;

public class MemberList implements Iterable<ProcessIdentifier>{

    private LinkedList<ProcessIdentifier> list;
    private Map<String, ProcState> stateMap;
    private Map<String, Long> timeMap;

    private static final Integer MAX_TIME_DIFFERENCE = 100;
    private static final Integer MIN_TIME_DIFFERENCE = 50;

    private static Logger logger = Logger.getLogger(MemberList.class);

    private Proc proc;

    public MemberList() {
        list = new LinkedList<ProcessIdentifier>();
        stateMap = new HashMap<String, ProcState>();
        timeMap = new HashMap<String, Long>();
    }

    public Integer remove(ProcessIdentifier processIdentifier){
        synchronized (this) {
            int pos = find(processIdentifier);
            if(pos != -1) {
                list.remove(pos);
                stateMap.remove(processIdentifier.getId());
                timeMap.remove(processIdentifier.getId());
                return pos;
            } else {
                return -1;
            }
        }
    }

    public void add(ProcessIdentifier processIdentifier){
        synchronized (this) {
            add(processIdentifier, TimeMachine.getTime());
        }
    }

    public void add(ProcessIdentifier processIdentifier, Long time) {
        synchronized (this) {
            list.add(processIdentifier);
            stateMap.put(processIdentifier.getId(), ProcState.available);
            timeMap.put(processIdentifier.getId(), time);
        }
    }

    public LinkedList<ProcessIdentifier> getList(){
        return this.list;
    }

    public ProcessIdentifier get(Integer pos){
        return this.list.get(pos);
    }

    public void set(Integer pos, ProcessIdentifier identifier) {
        synchronized (this) {
            list.set(pos, identifier);
        }
    }

    public ProcessIdentifier getFirst() {
        return list.getFirst();
    }

    public ProcessIdentifier getLast() {
        return list.getLast();
    }

    public ProcState getState(ProcessIdentifier identifier) {
        return stateMap.get(identifier.getId());
    }

    public Long getTime(ProcessIdentifier identifier) {
        return timeMap.get(identifier.getId());
    }

    public ProcessIdentifier getNextProcessIdentifier(Integer i){
        if(i > size() - 1)
            try {
                throw new Exception("Out of bound element access.");
            } catch (Exception e) {
                e.printStackTrace();
            }

        if(i < size() - 1)
            return  this.list.get(i+1);
        else
            return this.list.getFirst();

    }

    public Integer size(){
        return list.size();
    }

    public Iterator<ProcessIdentifier> iterator() {
        synchronized (this) {
            return list.iterator();
        }
    }

    public Integer find(ProcessIdentifier identifier) {
        synchronized (this) {
            int pos = 0;
            for(ProcessIdentifier proc : list) {
                if(proc.getId().equals(identifier.getId())) {
                    return pos;
                }
                ++pos;
            }
        }
        return -1;
    }

    public void updateProcessIdentifier(ProcessIdentifier identifier) {
        synchronized(this) {
            Integer pos = find(identifier);
            if(!timeMap.containsKey(identifier.getId())) { //add new entry to memberList
                add(identifier);
                return;
            }

            list.set(pos, identifier);
            timeMap.put(identifier.getId(), TimeMachine.getTime());
        }
    }

    public void setAsToBeDeleted(ProcessIdentifier identifier) {
        synchronized (this) {
            Integer pos = find(identifier);
            if(!stateMap.containsKey(identifier.getId())){
                logger.error("Wrong identifier to set as TBD: " + identifier.getId());
                return;
            }

            stateMap.put(identifier.getId(), ProcState.toBeDeleted);
        }
    }

    public void setAsAvailable(ProcessIdentifier identifier) {
        synchronized (this) {
            if(!stateMap.containsKey(identifier.getId())) {
                logger.error("Wrong identifier to set as Available: " + identifier.getId());
                return;
            }

            stateMap.put(identifier.getId(), ProcState.available);
        }
    }

    public boolean updateMemberList() {
        synchronized (this) {
            boolean flag = false;
            for(ProcessIdentifier identifier : list) {
                if(identifier.getId().equals(proc.getId())) {   //don't update itself
                    continue;
                }
                Long diff = TimeMachine.getTime() - timeMap.get(identifier.getId());
                if(diff > MAX_TIME_DIFFERENCE) {
                    remove(identifier);
                    return true;
                } else if(diff > MIN_TIME_DIFFERENCE) {
                    setAsToBeDeleted(identifier);
                    flag = true;
                }
            }
            return flag;
        }
    }

    public void setProc(Proc proc) {
        this.proc = proc;
    }
}
