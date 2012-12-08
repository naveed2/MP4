//package TFIDF;

import java.io.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: naveed
 * Date: 12/7/12
 * Time: 10:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class TFIDF {

    private ArrayList<String> keyValuePairs;
    private String outputFile;
    private File inputFile;

    public TFIDF(String inputFile, String outputFile){
        this.inputFile = getFile(inputFile);
        this.outputFile = outputFile;
    }

    public static void main(String[] args){

        //String inputFile = "/Users/naveed/Desktop/Courses/CS425/MP4/src/TFIDF/sampleFile.txt";
        //String outputFile = "/Users/naveed/Desktop/Courses/CS425/MP4/src/TFIDF/1";
        String inputFile = args[0]; String outputFile = args[1];
        TFIDF tf = new TFIDF(inputFile, outputFile);
        tf.writeTFIDF();
    }

    public File getFile(String filepath){
        return new File(filepath);
    }

    public void writeTFIDF(){

        getKeyValuePairs(inputFile);
        BufferedWriter bufferedWriter = null;

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        for(String pair:keyValuePairs){
            String doc = getKey(pair); String word = getValue(pair);
            Double tfidf = computeWordTFIDF(
                    getSingleWordCountInDoc(pair),
                    getTotalWordCountInDoc(doc),
                    getDocCountInCorpus(word));

            System.out.println(pair + " " + tfidf.toString());

            try {
                bufferedWriter.write( pair + "," + tfidf.toString() + '\n');
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

        }

        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public Double computeWordTFIDF(int singleWordCountInDoc, int totalWordCountInDoc, int docCountInCorpus) {
        //TODO the percentage should be in 100 or 1
        //System.out.println("singleWordCountInDoc:" + singleWordCountInDoc + "totalWordcountInDoc:" + totalWordCountInDoc + "docCountInCorpus:" + docCountInCorpus);
        return ((double)singleWordCountInDoc / (double)totalWordCountInDoc) * Math.log(1/(double)docCountInCorpus);
    }

    public ArrayList<String> getKeyValuePairs(File file){
        BufferedReader bufferedReader = null;
        ArrayList<String> keyValuePairs = new ArrayList<String>();
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String pair = null;
            while((pair = bufferedReader.readLine()) != null){
                keyValuePairs.add(pair);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        this.keyValuePairs = keyValuePairs;

        return keyValuePairs;
    }

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
}
