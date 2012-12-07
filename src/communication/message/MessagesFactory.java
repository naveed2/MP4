package communication.message;

import communication.FileIdentifierFactory;
import communication.message.Messages;
import filesystem.SDFS;
import membership.MemberList;
import membership.ProcState;
import misc.MiscTool;

import static communication.message.Messages.*;

public class MessagesFactory {

    private MessagesFactory() {

    }


//  This method generates join message with ProcessorIdentifier as an argument
    public static Message generateJoinMessage(ProcessIdentifier identifier) {
        JoinMessage joinMessage = JoinMessage.newBuilder().setJoinedMachine(identifier).build();
        return Message.newBuilder().
                setType(Messages.MessageType.Join).setJoinMessage(joinMessage).build();
    }

    //  This method generates join message with id, ip, port and timestamp as arguments
    public static Message generateJoinMessage(String id, String ip, Integer port, Integer timeStamp) {
        ProcessIdentifier identifier = ProcessIdentifier.newBuilder()
                                        .setId(id).setIP(ip).setPort(port).setTimestamp(timeStamp).build();
        return generateJoinMessage(identifier);
    }

    public static Message generateJoinMessage(String id, String address, Integer timeStamp) {
        String[] str = address.split(":");
        return generateJoinMessage(id, str[0], Integer.parseInt(str[1]), timeStamp);
    }

//    This method generate sync messages used to sync processes
    public static Message generateSyncProcessMessage(Integer timeStamp, ProcessIdentifier syncMachine, MemberList memberList) {
        SyncProcessesMessage.Builder syncMessageBuilder = SyncProcessesMessage.newBuilder();
        syncMessageBuilder.setSyncingMachine(syncMachine);

        for(int i=0; i<memberList.size(); ++i) {
            if(MiscTool.isTheSameIdentifier(memberList.get(i), syncMachine)) {
                syncMessageBuilder.addMembers(ProcessIdentifier.newBuilder()
                        .setId(syncMachine.getId()).setIP(syncMachine.getIP())
                        .setPort(syncMachine.getPort()).setTimestamp(timeStamp));
                continue;
            }
            if(memberList.getState(memberList.get(i)) == ProcState.available) {
                syncMessageBuilder.addMembers(memberList.get(i));
            }
//            syncMessageBuilder.addMembers(memberList.get(i));
        }


        SyncProcessesMessage syncMessage = syncMessageBuilder.build();
        return Message.newBuilder()
                .setType(MessageType.SyncProcesses).setSyncProcessesMessage(syncMessage).build();
    }

//    This method is used to generate message used to sync filelists
    public static Message generateSyncFileListMessage(Integer timeStamp, ProcessIdentifier syncMachine,
                                                      SDFS sdfs) {
        SyncFilesListMessage.Builder syncFileListMessageBuilder = SyncFilesListMessage.newBuilder();

        for(FileIdentifier fileIdentifier : sdfs.getFileList()) {
            if(!sdfs.isValid(fileIdentifier)) {
                continue;
            }

            FileIdentifier newFileIdentifier = FileIdentifierFactory.generateFileIdentifier(
                    fileIdentifier.getFileStoringProcess(), fileIdentifier.getFileName(),
                    sdfs.getFileState(fileIdentifier)
            );

            if(fileIdentifier.getFileStoringProcess().getId().equals(syncMachine.getId())) {
                syncFileListMessageBuilder.addFiles(newFileIdentifier).addTimestamp(timeStamp);
            } else {
                syncFileListMessageBuilder.addFiles(newFileIdentifier).addTimestamp(sdfs.getFileTimeStamp(fileIdentifier));
            }
        }
        return Message.newBuilder()
                .setType(MessageType.SyncFiles).setSyncFilesMessage(syncFileListMessageBuilder.build()).build();
    }

//    this method generates heartbeat messages
    public static Message generateHearBeatMessage(Integer timeStamp, ProcessIdentifier fromMachine) {
        HeartBeatMessage.Builder builder = HeartBeatMessage.newBuilder();
        HeartBeatMessage heartBeatMessage = builder.setFromMachine(ProcessIdentifier.newBuilder()
                .setId(fromMachine.getId()).setIP(fromMachine.getIP())
                .setPort(fromMachine.getPort()).setTimestamp(timeStamp).build()).build();

        return Message.newBuilder()
                .setType(MessageType.Heartbeat).setHeartBeatMessage(heartBeatMessage).build();
    }

//    this method generates message which is used to send messages
    public static Message generateSendToMessage(ProcessIdentifier sendToMachine) {
        SendToMessage sendToMessage = SendToMessage.newBuilder()
                .setSendToMachine(sendToMachine).build();
        return Message.newBuilder()
                .setType(MessageType.SendTo).setSendToMessage(sendToMessage).build();
    }

//    this method generates messages which is used to listen from other processes
    public static Message generateListenFromMessage(ProcessIdentifier listenFromMachine) {
        ListenFromMessage listenFromMessage = ListenFromMessage.newBuilder()
                .setListenFromMachine(listenFromMachine).build();
        return Message.newBuilder()
                .setType(MessageType.ListenFrom).setListenFrom(listenFromMessage).build();
    }

//    this method generates message which is used when process wants to get file.
    public static Message generateGetFileMessage(String SDFSfilepath, ProcessIdentifier requestingProcess){
        GetFileMessage getFileMessage = GetFileMessage.newBuilder()
                .setFilepath(SDFSfilepath).setRequestingProcess(requestingProcess).build();
        return Message.newBuilder().setType(MessageType.getFile)
                .setGetFileMessage(getFileMessage).build();
    }

    //    this method generates message which is used when process wants to put file.
    public static Message generatePutFileMessage(String SDFSfilepath, ProcessIdentifier storingProcess){
        PutFileMessage putFileMessage = PutFileMessage.newBuilder()
                .setFilepath(SDFSfilepath)
                .setStoringProcess(storingProcess).build();

        return Message.newBuilder()
                .setType(MessageType.putFile).setPutFileMessage(putFileMessage).build();
    }


    //    this method generates message which is used when process wants delete a file.
    public static Message generateDeletedFileMessage(String SDFSfilepath, ProcessIdentifier deletingProcess){
        DeleteFileMessage deleteFileMessage = DeleteFileMessage.newBuilder()
                .setFilepath(SDFSfilepath)
                .setDeletingProcess(deletingProcess).build();

        return Message.newBuilder()
                .setType(MessageType.deleteFile).setDeleteFileMessage(deleteFileMessage).build();
    }
// this method is used to generate messages that are send by the remote process when local process sends put request
    public static Message generateReadyToPutFileMessage(String SDFSfilepath, ProcessIdentifier storingProcess){
        ReadyToPutFileMessage readyToPutFileMessage = ReadyToPutFileMessage.newBuilder()
                .setFilepath(SDFSfilepath)
                .setStoringProcess(storingProcess).build();

        return Message.newBuilder()
                .setType(MessageType.readyToPut).setReadyToPutFileMessage(readyToPutFileMessage).build();
    }

    // this method is used to generate messages that are send by the remote process when local process sends get request
    public static Message generateReadyToGetFileMessage(String SDFSfilename, ProcessIdentifier storingProcess){
        ReadyToGetFileMessage readytoGetFileMessage = ReadyToGetFileMessage.newBuilder()
                .setFilepath(SDFSfilename)
                .setStoringProcess(storingProcess).build();

        return Message.newBuilder()
                .setType(MessageType.readyToGet)
                .setReadyToGetFileMessage(readytoGetFileMessage).build();
    }

    public static Message generateGetMessage(FileIdentifier fileIdentifier, ProcessIdentifier requestingProcess) {
        GetMessage getMessage = GetMessage.newBuilder()
                .setRequestingProcess(requestingProcess).setFileName(fileIdentifier.getFileName()).build();
        return Message.newBuilder()
                .setType(MessageType.get)
                .setGetMessage(getMessage).build();
    }





}
