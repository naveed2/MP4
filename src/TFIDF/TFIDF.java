//package TFIDF;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created with IntelliJ IDEA.
 * User: naveed
 * Date: 12/7/12
 * Time: 10:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class TFIDF {

    private ArrayList<String> keyValuePairs;
    private File inputFile;
    private String inputFileName;
    ArrayList<String> wordsList;

    public TFIDF(String inputFileName){
        this.inputFile = getFile(inputFileName);
        this.inputFileName = inputFileName;
        //this.outputFile = outputFile;
    }

    public static void main(String[] args){

        //String inputFile = "/Users/naveed/Desktop/Courses/CS425/MP4/src/TFIDF/sampleFile.txt";
        //String outputFile = "/Users/naveed/Desktop/Courses/CS425/MP4/src/TFIDF/1";
        String inputFile = args[0];
        //String outputFile = args[1];
        TFIDF tf = new TFIDF(inputFile);
        tf.computeTFIDF();
    }

    public File getFile(String filepath){
        return new File(filepath);
    }

    public void computeTFIDF(){

        for(String pair:keyValuePairs){
            String doc = getKey(pair); String word = getValue(pair);
            Double tfidf = computeWordTFIDF(
                    getSingleWordCountInDoc(pair),
                    getTotalWordCountInDoc(doc),
                    getDocCountInCorpus(word));

            System.out.println(pair + "," + tfidf.toString());
        }
    }

    public Double computeWordTFIDF(int singleWordCountInDoc, int totalWordCountInDoc, int docCountInCorpus) {
        //TODO the percentage should be in 100 or 1
        //System.out.println("singleWordCountInDoc:" + singleWordCountInDoc + "totalWordcountInDoc:" + totalWordCountInDoc + "docCountInCorpus:" + docCountInCorpus);
        return ((double)singleWordCountInDoc / (double)totalWordCountInDoc) * Math.log(1/(double)docCountInCorpus);
    }

//    public ArrayList<String> getKeyValuePairs(File file){
//        BufferedReader bufferedReader = null;
//        ArrayList<String> keyValuePairs = new ArrayList<String>();
//        try {
//            bufferedReader = new BufferedReader(new FileReader(file));
//            String pair = null;
//            while((pair = bufferedReader.readLine()) != null){
//                keyValuePairs.add(pair);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//
//        this.keyValuePairs = keyValuePairs;
//
//        return keyValuePairs;
//    }

    public int getTotalWordCountInDoc(String doc){
        int totalWordCountInDoc = 0;
        for( String pair:keyValuePairs){
            if(getKey(pair).equals(doc))
                totalWordCountInDoc++;
        }
        return totalWordCountInDoc;
    }

    public int getSingleWordCountInDoc(String pair){
        int singleWordCountInDoc = 0;
        for(String p:keyValuePairs){
            if(p.equals(pair))
                singleWordCountInDoc++;
        }
        return singleWordCountInDoc;
    }

    public int getDocCountInCorpus(String word){
        int docCountInCorpus = 0;
        for(String pair:keyValuePairs){
            if(getValue(pair).equals(word)){
                docCountInCorpus++;
            }
        }

        return docCountInCorpus;
    }

    public String getKey(String pair){
        return pair.split(",")[0];
    }

    public String getValue(String pair){
        return pair.split(",")[1];
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

    public ArrayList<String> getPairs(ArrayList<String> words){

            ArrayList<String> pairs = new ArrayList<String>();

            for(int i = 0; i < words.size(); i++){
                String word = words.get(i);
                int frequency = Collections.frequency(wordsList, word);
                pairs.add(word + ",(" + inputFileName + "," + Integer.toString(frequency) + "," + wordsList.size() + ")");
                words.remove(word);
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
