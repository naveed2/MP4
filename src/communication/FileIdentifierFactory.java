package communication;

import filesystem.FileState;

import static communication.message.Messages.*;


//This class is used to generate FileIdentifier messages, which are used as file identifiers in SDFS systems
public class FileIdentifierFactory {
    private FileIdentifierFactory() {

    }

    public static FileIdentifier generateFileIdentifier(ProcessIdentifier identifier, String fileName, FileState fileState) {
        return FileIdentifier.newBuilder().
                setFileName(fileName).setFileStoringProcess(identifier).setFileState(fileState.toString()).build();
    }
}
