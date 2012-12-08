package communication;

import filesystem.FileState;

import javax.xml.crypto.Data;
import java.util.Date;

import static communication.message.Messages.*;


//This class is used to generate FileIdentifier messages, which are used as file identifiers in SDFS systems
public class FileIdentifierFactory {
    private FileIdentifierFactory() {

    }

    public static FileIdentifier generateFileIdentifier(ProcessIdentifier identifier, String fileName, FileState fileState) {
        return generateFileIdentifier(identifier, fileName, fileState, new Date().getTime());
    }

    public static FileIdentifier generateFileIdentifier(ProcessIdentifier identifier, String fileName,
                                                        FileState fileState, long lastWriteTime) {
        return FileIdentifier.newBuilder().
                setFileName(fileName).setFileStoringProcess(identifier).
                setFileState(fileState.toString()).setLastWriteTime(lastWriteTime).build();
    }
}
