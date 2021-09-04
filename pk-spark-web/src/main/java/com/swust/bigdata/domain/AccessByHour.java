package com.swust.bigdata.domain;


//import lombok.*;

//@Setter
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//@ToString
public class AccessByHour {
    private String day;
    private String user;
    private String num;


    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public AccessByHour(String day, String user, String num) {
        this.day = day;
        this.user = user;
        this.num = num;
    }

    public AccessByHour() {
    }
}
