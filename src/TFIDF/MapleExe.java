//package TFIDF;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: naveed
 * Date: 12/9/12
 * Time: 5:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class MapleExe {

    private ArrayList<String> keyValuePairs;
    private File inputFile;
    private String inputFileName;
    ArrayList<String> words;

    public MapleExe(String inputFileName){
        this.inputFile = getFile(inputFileName);
        this.inputFileName = inputFileName;
    }

    public static void main(String[] args){

        //String inputFile = "/Users/naveed/Downloads/data/wtslv10.txt";
        //String outputFile = "/Users/naveed/Desktop/Courses/CS425/MP4/src/TFIDF/1";
        String inputFile = args[0];
        MapleExe mapleExe = new MapleExe(inputFile);
        mapleExe.getWords();
        mapleExe.getPairs();
        mapleExe.displayPairs();


    }

    public File getFile(String filepath){
        return new File(filepath);
    }

    public ArrayList<String> getWords(){

        ArrayList<String> words = new ArrayList<String>();

        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));

            String line = null;
            while( ( line = bufferedReader.readLine()) != null){
                Scanner wordScanner = new Scanner(line);
                while(wordScanner.hasNext()){
                    words.add(wordScanner.next());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        this.words = words;

        return words;
    }

    public ArrayList<String> getPairs(){

        ArrayList<String> pairs = new ArrayList<String>();
        ArrayList<String> processedWords = new ArrayList<String>();
        for(String word : words){
            if(!processedWords.contains(word)){
                processedWords.add(word);
                int frequency = Collections.frequency(words, word);
                pairs.add(word + ",(" + inputFileName + "," + Integer.toString(frequency) + "," + this.words.size() + ")");
            }
        }
        this.keyValuePairs = pairs;
        return pairs;



    }

    public void displayPairs(){
        for(String pair:keyValuePairs){
            System.out.println(pair);
        }
    }
}
