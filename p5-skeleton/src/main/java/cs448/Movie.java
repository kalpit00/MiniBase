package cs448;

import java.io.Serializable;
import java.util.List;

public class Movie implements Serializable {
    private Integer movieId;
    private String title;
    private String[] genres;

    public Movie(){}

    public Movie(Integer movieId, String title, String[] genre) {
        this.movieId = movieId;
        this.title = title;
        this.genres = genre;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    public static Movie parseMovie(String line){
        String[] cols = line.split("::");
        Movie m = new Movie();
        m.setMovieId(Integer.parseInt(cols[0]));
        m.setTitle(cols[1]);
        m.setGenres( cols[2].split("\\|"));
        return m;
    }
}
