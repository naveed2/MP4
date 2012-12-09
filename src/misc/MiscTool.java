package misc;

import com.google.protobuf.InvalidProtocolBufferException;
import com.sun.corba.se.spi.protocol.LocalClientRequestDispatcher;
import communication.message.Messages;
import membership.MemberList;
import membership.PIDComparator;
import membership.Proc;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static communication.message.Messages.ProcessIdentifier;

public class MiscTool {

    public static final Integer BUFFER_SIZE = 4096;

    private static Logger logger = Logger.getLogger(MiscTool.class);

    public static void callStaticMethod(Class className, String methodName) {
        Method method = null;
        try {
            method = className.getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(null);    //static method
            method.setAccessible(false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static String inputFileName(Scanner in) {
        System.out.print("Input the filename: ");
        return in.nextLine();
    }

    public static String inputSDFSRoot(Scanner in) {
        System.out.print("Input the root: ");
        return in.nextLine();
    }

    public static Integer inputPortNumber(Scanner in) {
        String str;
        int ret;
        while(true) {
            try {
                System.out.print("Input the port: ");
                str = in.nextLine();
                ret = Integer.parseInt(str);
                break;
            } catch (NumberFormatException ex) {
                System.out.println("Number format error");
            }
        }
        return ret;
    }

    public static String inputAddress(Scanner in) {
        String str;
        while(true) {
            System.out.print("Input the address:");
            str = in.nextLine();
            if(MiscTool.isIPAddress(str)) {
                break;
            }
        }
        return str;
    }

    public static boolean isIPAddress(String str) {
        String[] res = str.split(":");
        if(res.length!=2) {
            return false;
        }

        String ip;
        Integer port;
        try{
            port = Integer.parseInt(res[1]);
        } catch(NumberFormatException ex) {
            return false;
        }

        if(port<=0 || port >=65536) {
            return false;
        }

        ip = res[0];

        String regex;

        regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
                    + "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$" ;
        return ip.matches(regex);
    }

    public static void readFromInputStreamToOutputStream(InputStream is, OutputStream os) throws IOException {
        byte [] buffer = new byte[8096*4];
        int c;
        while((c = is.read(buffer)) != -1 ) {
            os.write(buffer, 0, c);
        }
        is.close();
        os.close();
    }

    public static boolean isTheSameIdentifier(ProcessIdentifier p1, ProcessIdentifier p2) {
        return p1.getId().equals(p2.getId());
    }

    public static void sleep(Integer ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            //do nothing
        }
    }

    private static Lock lock = new ReentrantLock();

    public static boolean requireToCreateFile(List<ProcessIdentifier> pidList, ProcessIdentifier pid, String fileName) {
        return requireToCreateFile(pidList, pid, fileName, pidList.size());
    }

    public static boolean requireToCreateFile(
            List<ProcessIdentifier> pidList, ProcessIdentifier pid, String fileName, Integer numJuice) {

        lock.lock();
        try {
            List<ProcessIdentifier> procIds = new LinkedList<ProcessIdentifier>(pidList);

            Collections.sort(procIds, new PIDComparator());

            Integer position = findPosition(procIds, pid);

            int fileHashCode = fileName.hashCode() % numJuice;
            if(fileHashCode<0) fileHashCode += numJuice;

            //        System.out.println(
            //                String.format("(fileName, numProcs, hashCode) = (%s, %s, %s)\n", fileName, numProcs, fileHashCode));
            return fileHashCode == position || fileHashCode == (position+1) % numJuice;
        } finally {
            lock.unlock();
        }
    }


    public static Integer findPosition(List<ProcessIdentifier> procIds, ProcessIdentifier pid) {
        Integer pos=0;
        for(ProcessIdentifier procId : procIds) {
            if(procId.getId().equals(pid.getId())) {
                return pos;
            }
            ++pos;
        }
        return -1;
    }

    public static void main(String[] args) throws InvalidProtocolBufferException {
        String regex = "^.*$";
        String str = "124";
        System.out.println(str.matches(regex));
    }


}
