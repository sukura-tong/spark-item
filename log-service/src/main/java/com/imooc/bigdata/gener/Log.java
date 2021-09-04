//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.imooc.bigdata.gener;

public class Log {
    private String version;
    private String os;
    private String ip;
    private long start;
    private long end;
    private long catagory;
    private String newId;
    private String user;
    private String behavior;
    private long traffic;

    public Log() {
    }

    public String toString() {
        return this.start + "\t" + this.end + "\t" + this.ip + "\t" + this.catagory + "\t" + this.newId + "\t" + this.user + "\t" + this.traffic + "\t" + this.version + "\t" + this.os;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOs() {
        return this.os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getStart() {
        return this.start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return this.end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getCatagory() {
        return this.catagory;
    }

    public void setCatagory(long catagory) {
        this.catagory = catagory;
    }

    public String getNewId() {
        return this.newId;
    }

    public void setNewId(String newId) {
        this.newId = newId;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getBehavior() {
        return this.behavior;
    }

    public void setBehavior(String behavior) {
        this.behavior = behavior;
    }

    public long getTraffic() {
        return this.traffic;
    }

    public void setTraffic(long traffic) {
        this.traffic = traffic;
    }
}
