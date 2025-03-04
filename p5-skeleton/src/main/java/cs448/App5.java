package cs448;

import org.apache.commons.cli.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.Optional;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;
import scala.Tuple4;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class App {

    private static Options options;
    private static CommandLineParser parser;
    private static SparkSession ssTest;

    public static void main(String[] args) {

        initOptions();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse( options, args );
            if (!line.hasOption("i")){
                printHelp();
                appExit();
            }

            String inPath = line.getOptionValue("i");
            String outPath = line.getOptionValue("o");

            String userFName = line.getOptionValue("u","users.dat");
            String moviesFName = line.getOptionValue("m","movies.dat");
            String ratingsFName  = line.getOptionValue("r", "ratings.dat");

            Conf conf = new Conf(inPath,outPath,userFName,moviesFName,ratingsFName);


            if (line.hasOption("warmup")){
                warmupExercise(conf);
            }
            else{
                // simple query parameter check
                if (!line.hasOption("q")){
                    System.out.println("Query parameters is not supplied. Please supply query parameters");
                    printHelp();
                    appExit();
                }
                String qps = line.getOptionValue("q");
                String [] params = qps.split(",");
                if (params.length < 2){
                    System.out.println("Query parameters is not formatted proparly");
                    printHelp();
                    appExit();
                }

                conf.appNum = Integer.parseInt(params[0]);
                conf.queryParams = params;

                // testing. parameters are ignored in this mode
                if (line.hasOption("test")){
                    conf.testMode = true;
                    System.out.println("Testing mode. Ignoring query parameters and using built-in values.");
                }

                run(conf);
            }
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "p5", options );
    }

    private static void appExit() {
        System.exit(0);
    }

    private static void initOptions(){
        options = new Options();
        options.addOption("i", "input-directory", true, "(required) path to input directory in HDFS");
        options.addOption("o", "output-directory", true, "path to output directory on HDFS");
        options.addOption("u", "user-data-filename", true, "User data filename");
        options.addOption("m", "movies-data-filename", true, "Movies data filename");
        options.addOption("r", "rating-data-filename", true, "Ratings data filename");
        options.addOption("q", "query-params", true, "Selects what query to execute and its parameters. Comma-separated format: QueryNumber,param1,param2,...");
        options.addOption("warmup", false,"Run warm-up exercise");
        options.addOption("test", false,"Run test - using fixed parameters");
        parser = new BasicParser();
        Logger.getLogger("org.apache.spark").setLevel(Level.ERROR);

    }

    private static void warmupExercise(Conf conf){
        System.out.println("*** WARM-UP EXERCISE ***");

        // Creating a Spark session
        SparkSession spark = SparkSession.builder().appName("CS 448 Project 5 -- Warmup Exercise").getOrCreate();

        String dataFiles[] = {conf.usersFName,conf.moviesFName,conf.ratingsFName};
        Dataset<String> data;

        // Reading, Parsing and counting lines for each of the data files
        JavaRDD<User> userRDD = spark.read().textFile(CS448Utils.resolveUri(conf.inPath,conf.usersFName)).cache()
                .javaRDD()
                .map(User::parseUser);
        long lineCount = userRDD.count();
        System.out.println("Total lines in data file ( "+ conf.usersFName +" ) : " + lineCount);

        JavaRDD<Movie> movieRDD = spark.read().textFile(CS448Utils.resolveUri(conf.inPath,conf.moviesFName)).cache()
                .javaRDD()
                .map(Movie::parseMovie);

        lineCount = movieRDD.count();
        System.out.println("Total lines in data file ( "+ conf.moviesFName +" ) : " + lineCount);

        JavaRDD<Rating> ratingRDD = spark.read().textFile(CS448Utils.resolveUri(conf.inPath,conf.ratingsFName)).cache()
                .javaRDD()
                .map(Rating::parseRating);

        lineCount = ratingRDD.count();
        System.out.println("Total lines in data file ( "+ conf.ratingsFName +" ) : " + lineCount);

        // Terminating the Spark session
        spark.stop();
    }

    private static void run(Conf conf){
        Project5 p5 = new Project5();
        if (conf.testMode){
            conf.outPath += "/test";
            ssTest = SparkSession.builder().appName("CS 448 Project 5 -- Test session").getOrCreate();
            String testPath = CS448Utils.resolveUri(conf.outPath);
            try {
                FileSystem hdfs = FileSystem.get(ssTest.sparkContext().hadoopConfiguration());
                if (hdfs.exists(new Path(testPath)))
                    hdfs.delete(new Path(testPath), true);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

        switch (conf.appNum){
            default:
                assert(conf.appNum == 1);
                try {
                    if (conf.testMode){
                        conf.q1Occupation = CS448Utils.TEST_q1Occupation;
                        conf.q1Rating = CS448Utils.TEST_q1Rating;
                    }
                    else{
                        conf.q1Occupation = Integer.parseInt(conf.queryParams[1]);
                        conf.q1Rating = Integer.parseInt(conf.queryParams[2]);
                    }
                }
                catch (Exception e){
                    System.out.println(String.format("Error parsing Query %d Query parameters format error.",conf.appNum));
                    printHelp();
                    appExit();
                }
                p5.runSparkApp1(conf);
                break;
        }

        if (conf.testMode){
            testQuery(conf);
        }
    }

    private static void testQuery(Conf conf) {
        
        // Test Query Output
        boolean res = false;
        int queryNum = conf.appNum;
        System.out.println(String.format("Running test for Query %d",queryNum));
        
        ssTest = SparkSession.builder().appName(String.format("CS 448 Project 5 -- Test session for Q%d",queryNum)).getOrCreate();

        // Compare Results
        JavaPairRDD<String,Long> qResultRDDTest = ssTest.read().textFile(CS448Utils.getTestUri(queryNum)).javaRDD().mapToPair(l -> new Tuple2<String,Long>(l,1L));

        JavaPairRDD<String,Long> qResultRDD = ssTest.read().textFile(CS448Utils.resolveUri(conf.outPath + String.format("/query-%d",queryNum))).javaRDD().mapToPair(l -> new Tuple2<String,Long>(l,1L));

        JavaPairRDD<String, Tuple2<Optional<Long>,Optional<Long>>> joined = qResultRDD.fullOuterJoin(qResultRDDTest).cache();
        
        long test_cnt = qResultRDDTest.count();
        long ans_cnt = qResultRDD.count();
        long join_cnt = joined.count();
        res = (test_cnt == ans_cnt) && test_cnt == join_cnt;
        

        if (res){
            System.out.println(String.format("Test for Query %d PASSED",queryNum));
        }
        else{
            System.out.println(String.format("Test for Query %d FAILED!!!",queryNum));
        }
        ssTest.stop();
    }

    public static class Conf implements Serializable {

        String inPath,outPath,usersFName, moviesFName, ratingsFName;
        int appNum;
        String [] queryParams;
        int q1Occupation,q1Rating;
        int q2Age,q2Rating;
        String q3Genre;
        int q3Rating,q3Movies;
        int q4Age;
        boolean testMode;

        public Conf(String inPath, String outPath, String usersFName, String moviesFName, String ratingsFName) {
            this.inPath = inPath;
            this.outPath = outPath;
            this.usersFName = usersFName;
            this.moviesFName = moviesFName;
            this.ratingsFName = ratingsFName;
            testMode = false;
        }

        public String getInPath() {
            return inPath;
        }

        public void setInPath(String inPath) {
            this.inPath = inPath;
        }

        public String getOutPath() {
            return outPath;
        }

        public void setOutPath(String outPath) {
            this.outPath = outPath;
        }

        public String getUserFName() {
            return usersFName;
        }

        public void setUserFName(String userFName) {
            this.usersFName = userFName;
        }

        public String getMoviesFName() {
            return moviesFName;
        }

        public void setMoviesFName(String moviesFName) {
            this.moviesFName = moviesFName;
        }

        public String getRatingsFName() {
            return ratingsFName;
        }

        public void setRatingsFName(String ratingsFName) {
            this.ratingsFName = ratingsFName;
        }


    }
}
