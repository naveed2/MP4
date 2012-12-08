package communication;


//This stores the type of fileMission

import communication.message.Messages;

public class FileMission {

    public enum MissionType{
        send, get
    }

    private MissionType missionType;
    private String fileHeader;
    private String fileName;
    private Long lastTimeWrite;

    public FileMission(MissionType missionType) {
        this.missionType = missionType;
    }

    public void init(String fileName) {
        this.fileName = fileName;
        Integer hashCode = fileName.hashCode();
        hashCode = Math.abs(hashCode);
        this.fileHeader = String.format("%010d", hashCode);
    }

    public void init(Messages.FileIdentifier fid) {
        init(fid.getFileName());
        lastTimeWrite = fid.getLastWriteTime();
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileHeader() {
        return fileHeader;
    }

    public Long getLastTimeWrite() {
        return lastTimeWrite;
    }

    public boolean isGetMission() {
        return missionType == MissionType.get;
    }
}
