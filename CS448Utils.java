package cs448;

import static cs448.CS448Constants.HDFS_URI;
import static cs448.CS448Constants.TESTCASE_PATH;

public class CS448Utils {

    //Testing values for Q1
    public static int TEST_q1Rating = 3;
    public static int TEST_q1Occupation = 12;

    //Testing values for Q2    
    public static int TEST_q2Age = 18;
    public static int TEST_q2Rating = 3;
    

    public static String resolveUri(String path){
        StringBuilder sb = new StringBuilder();
        sb.append(HDFS_URI);
        sb.append('/');
        sb.append(path);
        return sb.toString();
    }

    public static String resolveUri(String dirPath, String filename){
        StringBuilder sb = new StringBuilder();
        sb.append(dirPath);
        sb.append('/');
        sb.append(filename);
        return resolveUri(sb.toString());
    }

    public static String getTestUri(int i){
        return resolveUri(String.format(TESTCASE_PATH,i));
    }

}
