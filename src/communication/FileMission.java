package communication;


//This stores the type of fileMission

public class FileMission {

    public enum MissionType{
        send, get
    }

    private MissionType missionType;
    private String fileName;

    public FileMission(MissionType missionType) {
        this.missionType = missionType;
    }

    public void init(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isGetMission() {
        return missionType == MissionType.get;
    }
}
