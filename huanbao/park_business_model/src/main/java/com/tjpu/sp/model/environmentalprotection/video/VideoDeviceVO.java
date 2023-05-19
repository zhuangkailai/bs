package com.tjpu.sp.model.environmentalprotection.video;

public class VideoDeviceVO {
    private String pkVediodeviceid;

    private String vediodevicename;

    private String vediodevicenumber;

    private String vediodeviceposition;

    private String ip;

    private String username;

    private String userpassword;

    private String deviceport;

    public String getPkVediodeviceid() {
        return pkVediodeviceid;
    }

    public void setPkVediodeviceid(String pkVediodeviceid) {
        this.pkVediodeviceid = pkVediodeviceid == null ? null : pkVediodeviceid.trim();
    }

    public String getVediodevicename() {
        return vediodevicename;
    }

    public void setVediodevicename(String vediodevicename) {
        this.vediodevicename = vediodevicename == null ? null : vediodevicename.trim();
    }

    public String getVediodevicenumber() {
        return vediodevicenumber;
    }

    public void setVediodevicenumber(String vediodevicenumber) {
        this.vediodevicenumber = vediodevicenumber == null ? null : vediodevicenumber.trim();
    }

    public String getVediodeviceposition() {
        return vediodeviceposition;
    }

    public void setVediodeviceposition(String vediodeviceposition) {
        this.vediodeviceposition = vediodeviceposition == null ? null : vediodeviceposition.trim();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getUserpassword() {
        return userpassword;
    }

    public void setUserpassword(String userpassword) {
        this.userpassword = userpassword == null ? null : userpassword.trim();
    }

    public String getDeviceport() {
        return deviceport;
    }

    public void setDeviceport(String deviceport) {
        this.deviceport = deviceport == null ? null : deviceport.trim();
    }
}