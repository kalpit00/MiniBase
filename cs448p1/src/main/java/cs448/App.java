package cs448;

import java.io.*;

public class App
{
    public static void main( String[] args )
    {
        String input_file_path = args[0];
        int backend = Integer.parseInt(args[1]);
        String id = args[2];
        project1 p = new project1();
        long startts;
        String[] column_names = {"primaryName", "birthYear", "deathYear", "primaryProfession"};
        try {
            switch (backend){
                case 0:
                    startts =  System.nanoTime();
                    System.out.println(p.select_file(input_file_path,id,column_names));
                    System.out.println("Elapsed time in (ns): "+(System.nanoTime()-startts));
                    break;
                case 1:
                    startts =  System.nanoTime();
                    p.load_mainmemory(input_file_path);
                    System.out.println("Load: elapsed time in (ns): "+(System.nanoTime()-startts));
                    startts =  System.nanoTime();
                    System.out.println(p.select_mainmemory(id,column_names));
                    System.out.println("Query: elapsed time in (ns): "+(System.nanoTime()-startts));
                    break;
                case 2:
                    startts =  System.nanoTime();
                    p.load_mapdb(input_file_path);
                    System.out.println("Load: elapsed time in (ns): "+(System.nanoTime()-startts));
                    startts =  System.nanoTime();
                    System.out.println(p.select_mapdb(id,column_names));
                    System.out.println("Query: elapsed time in (ns): "+(System.nanoTime()-startts));
                    break;
                default:
                    System.out.println("Syntax: project1 path/to/datafile 0|1|2 id");
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        catch (OutOfMemoryError e){
            System.gc();
            e.printStackTrace();
        }
        finally {
            p.db.close();
        }
    }

}
