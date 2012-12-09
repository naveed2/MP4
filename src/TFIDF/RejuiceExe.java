//package TFIDF;
//
///**
// * Created with IntelliJ IDEA.
// * User: naveed
// * Date: 12/9/12
// * Time: 5:30 AM
// * To change this template use File | Settings | File Templates.
// */
//public class RejuiceExe {
//
//    public void computeTFIDF(){
//
//        for(String pair:keyValuePairs){
//            String doc = getKey(pair); String word = getValue(pair);
//            Double tfidf = computeWordTFIDF(
//                    getSingleWordCountInDoc(pair),
//                    getTotalWordCountInDoc(doc),
//                    getDocCountInCorpus(word));
//
//            System.out.println(pair + "," + tfidf.toString());
//        }
//    }
//
//    public int getDocCountInCorpus(String word){
//        int docCountInCorpus = 0;
//        for(String pair:keyValuePairs){
//            if(getValue(pair).equals(word)){
//                docCountInCorpus++;
//            }
//        }
//
//        return docCountInCorpus;
//    }
//
//    public String getKey(String pair){
//        return pair.split(",")[0];
//    }
//
//    public String getValue(String pair){
//        return pair.split(",")[1];
//    }
//}
