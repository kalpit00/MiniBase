package cs448;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentMap;


public class project1 {

    /* Data structure for Main-memory back-end */
    HashMap<String,Dictionary> mm_map = new HashMap<String,Dictionary>();
    // here, key is row ID in String. The hashmap stores each row and every col
    // Dictionary is <col, col_value> as <key, value> pairs for each

    /* Data structure for MapDB persistent storage*/
    String dbfile = "data.db";
    DB db = DBMaker.fileDB(dbfile).make();

    // use this for MapDB storage
    ConcurrentMap mapdb = db.hashMap("map").make();

    void load_mainmemory(String file_path) throws IOException {
        /** Put your code here **/
        File file = new File(file_path);
        String row = "";
        HashMap<Integer, String> map = new HashMap<>();
        map.put(0, "nconst");
        map.put(1, "primaryName");
        map.put(2, "birthYear");
        map.put(3, "deathYear");
        map.put(4, "primaryProfession");
        map.put(5, "knownForTitles");

        try (BufferedReader bfr = new BufferedReader(new FileReader(file))) {
            while ((row = bfr.readLine()) != null) {
                Dictionary<String, String> dictionary = new Hashtable<>();
                String[] valuesInRow = row.split("\t");
                for (int i = 0; i < valuesInRow.length; i++) {
                    dictionary.put(map.get(i), valuesInRow[i]);
                }
                mm_map.put(dictionary.get(map.get(0)), dictionary);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    void load_mapdb(String file_path) throws IOException{
        /** Put your code here **/
        File file = new File(file_path);
        String row = "";
        HashMap<Integer, String> map = new HashMap<>();
        map.put(0, "nconst");
        map.put(1, "primaryName");
        map.put(2, "birthYear");
        map.put(3, "deathYear");
        map.put(4, "primaryProfession");
        map.put(5, "knownForTitles");

        try (BufferedReader bfr = new BufferedReader(new FileReader(file))) {
            while ((row = bfr.readLine()) != null) {
                String[] valuesInRow = row.split("\t");
                Dictionary<String, String> dictionary = new Hashtable<>();
                for (int i = 0; i < valuesInRow.length; i++) {
                    dictionary.put(map.get(i), valuesInRow[i]);
                }
                mapdb.put(dictionary.get(map.get(0)), dictionary);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    String select_file(String file_path, String key, String[] column_names) throws IOException {
        /** Put your code here **/
        // key = ID of row = nm0000001
        // open file, .tsv is tab separated file, get all lines, match the first word of every line to key
        // run a for loop through column_names array, to get those values
        // return a string concatenated with those values
        StringBuilder result = new StringBuilder();
        File file = new File(file_path);
        String row = "";
        HashMap<String, Integer> map = new HashMap<>();
        map.put("nconst", 0);
        map.put("primaryName", 1);
        map.put("birthYear", 2);
        map.put("deathYear", 3);
        map.put("primaryProfession", 4);
        map.put("knownForTitles", 5);
        try (BufferedReader bfr = new BufferedReader(new FileReader(file))) {
            while ((row = bfr.readLine()) != null) {
                String[] valuesInRow = row.split("\t");
                if (valuesInRow[0].equals(key)) {
                    for (String col : column_names) {
                        result.append(valuesInRow[map.get(col)]);
                        result.append("\t");
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result.toString().trim();
        // This is O(number of records) algo as it will check every row to match the key and expand its columns
        // once match found
    }
    

    String select_mainmemory(String key, String[] column_names){
        /** Put your code here **/
        StringBuilder result = new StringBuilder();
        Dictionary dictionary = mm_map.get(key);
        for (String col : column_names) {
            result.append(dictionary.get(col));
            result.append("\t");
        }
        return result.toString().trim();
        // This is O(1) algo as mm_map is a hashmap, we pass in the nconst as key and get the row in O(1)
    }
    String select_mapdb(String key, String[] column_names){
        /** Put your code here **/
        StringBuilder result = new StringBuilder();
        Dictionary dictionary = (Dictionary) mapdb.get(key);
        for (String col : column_names) {
            result.append(dictionary.get(col));
            result.append("\t");
        }
        return result.toString().trim();
        // This is also O(1) algo, as mapdb gets record associated with the nconst key in O(1)
    }

    int fastestLoad(){
        // 1: Main-memory Load
        // 2: MapDB Load
        /** Put your code here **/
        return 1;
    }

    int fastestSelect(){
        // 0: File Select
        // 1: Main-memory Select
        // 2: MapDB Select
        /** Put your code here **/
        return 1;
    }
}
