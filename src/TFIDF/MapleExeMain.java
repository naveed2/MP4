package TFIDF;

public class MapleExeMain {

    public static void main(String[] args){
        //args[0] accepts input file name and args[1] accept output file name
        String inputFile = args[0];
        String outputFile = args[1];

        Maple_exe maple_exe = new Maple_exe(inputFile, outputFile);
        maple_exe.writePairs(maple_exe.getWords(maple_exe.inputFile));
        try {
            if(args.length == 3) {
                System.out.println("maple-exe is tired and is sleeping for " + Long.parseLong(args[2]) + "s.");
            Thread.sleep(Long.parseLong(args[2])*1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
