package cs448;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.*;
import org.apache.spark.sql.sources.In;
import scala.Tuple2;

public class Project5 {
    
    /*** Some pointers to implement queries:
     * Follow the warm up exercise and Spark SQL documentation to get started
     * Create a Spark Session.
     * Create RDD for the different tables.
     * Create a Data Frame.
     * Create a View.
     * Query using Spark SQL.
     * Save the output in HDFS.
     * Remember Stop spark session.

    Note: These are just suggestions, you do not need to strictly adhere to these.
    ***/

    public void runSparkApp1(App.Conf conf){
        System.out.println("Running Your First Spark App!");
        // Write your code here

        // Create a Spark Session.
        SparkSession sparkSession = SparkSession.builder().appName("CS448 Project 5 -- Query 1").getOrCreate();

        // Write data processing code here
        // Using Spark SQL
        JavaRDD<User> userJavaRDD = sparkSession.read().textFile(
                CS448Utils.resolveUri(conf.inPath, conf.usersFName))
                .cache().javaRDD().map(User::parseUser);

        JavaRDD<Movie> movieJavaRDD = sparkSession.read().textFile(
                CS448Utils.resolveUri(conf.inPath, conf.moviesFName))
                .cache().javaRDD().map(Movie::parseMovie);

        JavaRDD<Rating> ratingJavaRDD = sparkSession.read().textFile(
                CS448Utils.resolveUri(conf.inPath, conf.ratingsFName))
                .cache().javaRDD().map(Rating::parseRating);

        Dataset<Row> userDataset = sparkSession.createDataFrame(userJavaRDD, User.class);
        Dataset<Row> movieDataset = sparkSession.createDataFrame(movieJavaRDD, Movie.class);
        Dataset<Row> ratingDataset = sparkSession.createDataFrame(ratingJavaRDD, Rating.class);

        userDataset.createOrReplaceTempView("users");
        movieDataset.createOrReplaceTempView("movies");
        ratingDataset.createOrReplaceTempView("ratings");

        String query =
                "select distinct t.title " +
                        "from (" +
                        "select title, rating, occupation " +
                        "from movies m " +
                        "join ratings r on m.movieid=r.movieid " +
                        "join users u on u.userid=r.userid) t " +
                        "where t.rating>=" + conf.q1Rating + " " +
                        "and t.occupation=" + conf.q1Occupation;


        Dataset<Row> result = sparkSession.sql(query);
        result.javaRDD().map(r -> r.getString(0))
                .saveAsTextFile(CS448Utils.resolveUri(conf.outPath,"query-1"));
        sparkSession.stop();
    }
}
