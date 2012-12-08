package TFIDF;

/**
 * Created with IntelliJ IDEA.
 * User: naveed
 * Date: 12/7/12
 * Time: 11:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class tfidfmain {

    public static void main(String[] args){

        String hello = "doc1,hello";

        System.out.println(hello.split(",")[0]);
        System.out.println(hello);
        System.out.println(hello.split(",")[1]);
        TFIDF tf = new TFIDF("/Users/naveed/Desktop/Courses/CS425/MP4/src/TFIDF/sampleFile.txt");
        tf.writeTFIDF();
    }


}
