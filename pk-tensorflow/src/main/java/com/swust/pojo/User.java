package com.swust.pojo;

public class User {
    private String uname;
    private String uid;
    private String uage;

    private int getResult(String uname) {
        if (uname == this.uname) {
            return 1;
        }
        return 0;
    }

    public String getUid(String uname) {
        return getResult(uname) == 1 ? this.uid : "88888";
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getUname() {
        return this.uname;
    }


}
