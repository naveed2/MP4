package main;

import java.util.HashMap;
import java.util.Map;

public class CommandMap {

    private static CommandMap instance = new CommandMap();
    private Map<String, String> stringToFuncName;

    private static final String CMD_START = "start";
    private static final String FUNC_START = "start";

    private static final String CMD_INIT = "init";
    private static final String FUNC_INIT = "initProc";

    private static final String CMD_JOIN = "join";
    private static final String FUNC_JOIN = "joinGroup";

    private static final String CMD_SHOW = "show";
    private static final String FUNC_SHOW = "showMemberList";

    private static final String CMD_SHOW_FILE = "showfile";
    private static final String FUNC_SHOW_FILE = "showFileList";

    private static final String CMD_PUT = "put";
    private static final String FUNC_PUT = "putFile";

    private static final String CMD_GET = "get";
    private static final String FUNC_GET = "getFile";

    private static final String CMD_HELP = "help";
    private static final String FUNC_HELP = "printHelp";

    private static final String CMD_QUIT = "quit";
    private static final String FUNC_QUIT ="quit";

    private static final String CMD_DEL = "del";
    private static final String FUNC_DEL = "deleteFile";

    private CommandMap() {

    }

    public static CommandMap getInstance() {
        return instance;
    }

    public synchronized CommandMap initialize() {
        instance = new CommandMap();
        stringToFuncName = new HashMap<String, String>();

        stringToFuncName.put(CMD_START, FUNC_START);
        stringToFuncName.put(CMD_INIT, FUNC_INIT);
        stringToFuncName.put(CMD_JOIN, FUNC_JOIN);
        stringToFuncName.put(CMD_SHOW, FUNC_SHOW);
        stringToFuncName.put(CMD_SHOW_FILE, FUNC_SHOW_FILE);
        stringToFuncName.put(CMD_PUT, FUNC_PUT);
        stringToFuncName.put(CMD_HELP, FUNC_HELP);
        stringToFuncName.put(CMD_QUIT, FUNC_QUIT);
        stringToFuncName.put(CMD_DEL, FUNC_DEL);
        stringToFuncName.put(CMD_GET, FUNC_GET);
        stringToFuncName.put(CMD_QUIT, FUNC_QUIT);

        return this;
    }

    private static final String FORMAT_STRING = "%-25s%-25s\n";

    public static void printHelp() {
        System.out.printf(FORMAT_STRING, "COMMAND", "USAGE");
        System.out.printf(FORMAT_STRING, CMD_QUIT, "quit");
    }

    public synchronized String findCommand(String cmd) {
        return stringToFuncName.get(cmd);
    }
}
