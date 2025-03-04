package cs448;

import java.io.Serializable;

public class Rating implements Serializable {
    private Integer userId;
    private Integer movieId;
    private Integer rating;
    private Long time;

    public Rating(){}

    public Rating(Integer userId, Integer movieId, Integer rating, Long time) {
        this.userId = userId;
        this.movieId = movieId;
        this.rating = rating;
        this.time = time;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public static Rating parseRating(String line){
        String[] cols = line.split("::");
        Rating r = new Rating();
        r.setUserId(Integer.parseInt(cols[0]));
        r.setMovieId(Integer.parseInt(cols[1]));
        r.setRating(Integer.parseInt(cols[2]));
        r.setTime(Long.parseLong(cols[3]));
        return r;
    }
}
