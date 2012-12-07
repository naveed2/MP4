package filesystem;

import java.util.Iterator;
import java.util.LinkedList;

import communication.message.Messages.FileIdentifier;

//This class is used to implement filelist

public class FileList implements Iterable<FileIdentifier> {

    private LinkedList<FileIdentifier> fileList;

    public FileList() {
        fileList = new LinkedList<FileIdentifier>();
    }

    void removeFile(FileIdentifier fileIdentifier){
        synchronized (this) {
            int pos = find(fileIdentifier);
            if(pos != -1) {
//                fileList.remove(fileList.get(pos));
                fileList.remove(pos);
            }
        }
    }

    void addFile(FileIdentifier fileIdentifier){
        synchronized (this) {
            this.fileList.add(fileIdentifier);
        }
    }

    public FileList get(){
        return this;
    }

    public FileIdentifier getFileLocation(String filename){
        for( FileIdentifier f : this.fileList) {
            if(f.getFileName().equals(filename))
                return f;
            else
                System.out.println("File not present in the system.");
        }

        return null;
    }

    public Integer length(){
        return this.length();
    }

    public Integer find(FileIdentifier identifier) {
        synchronized (this) {
            for(int i=0; i<fileList.size(); ++i) {
                FileIdentifier tmp = fileList.get(i);
                if(theSameFileIdentifier(tmp, identifier)) {
                    return i;
                }
            }
            return -1;
        }
    }

    private boolean theSameFileIdentifier(FileIdentifier f1, FileIdentifier f2) {
        return f1.getFileStoringProcess().getId().equals(f2.getFileStoringProcess().getId())
                && f1.getFileName().equals(f2.getFileName());

    }


    public Iterator<FileIdentifier> iterator() {
        synchronized (this){
            return fileList.iterator();
        }
    }

    public Integer size() {
        return fileList.size();
    }
}
