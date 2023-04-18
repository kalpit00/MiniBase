package cs448;

import java.io.Serializable;

public class User implements Serializable {
    private Integer userId;
    private String gender;
    private Integer age;
    private Integer occupation;
    private String zipcode;

    public User(){}

    public User(Integer userId, String gender, Integer age, Integer occupation, String zipcode) {
        this.userId = userId;
        this.gender = gender;
        this.age = age;
        this.occupation = occupation;
        this.zipcode = zipcode;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getOccupation() {
        return occupation;
    }

    public void setOccupation(Integer occupation) {
        this.occupation = occupation;
    }


    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public static User parseUser(String line){
        String[] cols = line.split("::");
        User u = new User();
        u.setUserId(Integer.parseInt(cols[0]));
        u.setGender(cols[1]);
        u.setAge(Integer.parseInt(cols[2]));
        u.setOccupation(Integer.parseInt(cols[3]));
        u.setZipcode(cols[4]);
        return u;
    }
}
