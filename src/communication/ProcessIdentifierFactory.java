package communication;

import static communication.message.Messages.ProcessIdentifier;

public class ProcessIdentifierFactory {
    private ProcessIdentifierFactory() {

    }
     // This method is used to generate ProcessIdnetifer message which is used to identify proceses in the system
    public static ProcessIdentifier generateProcessIdentifier(
            String id, String address, Integer port, Integer timeStamp) {
        ProcessIdentifier identifier;
        identifier = ProcessIdentifier.newBuilder()
                .setId(id).setIP(address).setPort(port).setTimestamp(timeStamp).build();
        return identifier;
    }
}
