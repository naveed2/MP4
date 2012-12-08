//package TFIDF;

import java.io.*;
import java.util.*;

public class Maple_exe {

    String outputFile;
    String inputFile;
    ArrayList<String> wordsList;
    HashMap<String, String> pairs;

    public Maple_exe(String inputFile, String outputFile){
        this.outputFile = outputFile;
        this.inputFile = inputFile;
    }

    public void write(String string){
        BufferedWriter bufferedWriter = null;
        try {

            System.out.println(string);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public ArrayList<String> getWords(String inputFile){

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

        wordsList = words;

        return words;
    }

    public void writePairs(ArrayList<String> words){
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(outputFile));

            for(int i = 0; i < words.size(); i++){
                String word = words.get(i);
                int frequency = Collections.frequency(wordsList, word);
                bufferedWriter.write(word + ',' + frequency + '\n');
                words.remove(word);
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }




}
