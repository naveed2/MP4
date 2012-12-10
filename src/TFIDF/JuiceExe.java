//package TFIDF;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
* Created with IntelliJ IDEA.
* User: naveed
* Date: 12/9/12
* Time: 5:30 AM
* To change this template use File | Settings | File Templates.
*/
public class JuiceExe {

    HashMap<String, String[]> pairs = new HashMap<String, String[]>();
    int totaldoc;





    public static void main(String[] args){

//        String[] args1 = new String[1];


        JuiceExe juiceExe = new JuiceExe();

        juiceExe.getAllPairs(args);
        juiceExe.getTotalDoc();
        juiceExe.displayTFIDF();

    }

    public void getAllPairs(String[] inputFiles){

        for(String inputFile:inputFiles){
            ArrayList<String[]> parsedPairs = parsePairs(inputFile);
            for(String[] pair:parsedPairs){




            String key = pair[0];
            String[] value = new String[4];
            System.arraycopy(pair, 1, value, 0, pair.length-1);

            if(pairs.containsKey(key)){

                value[3] = Integer.toString(Integer.parseInt(value[3]) + 1);
            }
//            for(String s:value){
//                System.out.println(s);
//            }
            pairs.put(key, value);
            }
        }
    }

    public int getTotalDoc(){
        int totalDoc = 0;
        ArrayList<String> processedNames = new ArrayList<String>();
        for(Map.Entry<String, String[]> pair : pairs.entrySet()){
            String docName = pair.getValue()[0];
            if(!processedNames.contains(docName)){
                processedNames.add(docName);
                totalDoc++;
            }

        }
        this.totaldoc = totalDoc;
        return totalDoc;
    }



    public void displayTFIDF(){
        for(Map.Entry<String, String[]> pair : pairs.entrySet()){
            String[] value = pair.getValue();
            Double tfidf = computeTFIDF(Integer.parseInt(value[1]), Integer.parseInt(value[2]), Integer.parseInt(value[3]));
//            System.out.println(Integer.parseInt(value[1]) +" " + Integer.parseInt(value[2]) +" " + Integer.parseInt(value[3]));
            System.out.println("(" + pair.getKey() + "," + pair.getValue()[0] + ")," + Double.toString(tfidf));
        }
    }

    public Double computeTFIDF(int singleWordCountInDoc, int totalWordCountInDoc, int docCountInCorpus) {


        return ((double)singleWordCountInDoc / (double)totalWordCountInDoc) * Math.log((double)totaldoc/(double)docCountInCorpus);
    }

    public ArrayList<String[]> parsePairs(String inputFile){
        ArrayList<String[]> parsedPairs = new ArrayList<String[]>();

        BufferedReader bufferedReader = null;
        String[] parsedPair = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(inputFile));

            String line = null;
            while( ( line = bufferedReader.readLine()) != null){
                line = line.replaceAll("[\\(\\)]", "");
                //System.out.println(line);
                parsedPair = line.split(",");
                parsedPairs.add(parsedPair);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return parsedPairs;
    }



    public String getKey(String pair){
        return pair.split(",")[0];
    }

    public String getValue(String pair){
        return pair.split(",")[1];
    }


}
