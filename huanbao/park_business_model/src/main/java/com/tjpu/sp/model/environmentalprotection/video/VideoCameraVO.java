package com.tjpu.sp.model.environmentalprotection.video;

import java.util.Date;

public class VideoCameraVO {
    private String pkVediocameraid;

    private String fkVediodeviceid;

    private String vediocameraname;

    private String vediocameranumber;

    private String vediocameraposition;

    private Date producedate;

    private Double longitude;

    private Double latitude;

    private String vediomanufactor;

    private String fkMonitorpointoroutputid;

    private String fkMonitorpointtypecode;

    private String rtsp;

    private Integer isshow;

    private Integer vediocameratype;

    private String fkVediocameracategory;

    private String fkPollutionid;

    private Integer status;

    private Date alarmtime;

    private String pushurl;

    public String getPkVediocameraid() {
        return pkVediocameraid;
    }

    public void setPkVediocameraid(String pkVediocameraid) {
        this.pkVediocameraid = pkVediocameraid == null ? null : pkVediocameraid.trim();
    }

    public String getFkVediodeviceid() {
        return fkVediodeviceid;
    }

    public void setFkVediodeviceid(String fkVediodeviceid) {
        this.fkVediodeviceid = fkVediodeviceid == null ? null : fkVediodeviceid.trim();
    }

    public String getVediocameraname() {
        return vediocameraname;
    }

    public void setVediocameraname(String vediocameraname) {
        this.vediocameraname = vediocameraname == null ? null : vediocameraname.trim();
    }

    public String getVediocameranumber() {
        return vediocameranumber;
    }

    public void setVediocameranumber(String vediocameranumber) {
        this.vediocameranumber = vediocameranumber == null ? null : vediocameranumber.trim();
    }

    public String getVediocameraposition() {
        return vediocameraposition;
    }

    public void setVediocameraposition(String vediocameraposition) {
        this.vediocameraposition = vediocameraposition == null ? null : vediocameraposition.trim();
    }

    public Date getProducedate() {
        return producedate;
    }

    public void setProducedate(Date producedate) {
        this.producedate = producedate;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getVediomanufactor() {
        return vediomanufactor;
    }

    public void setVediomanufactor(String vediomanufactor) {
        this.vediomanufactor = vediomanufactor == null ? null : vediomanufactor.trim();
    }

    public String getFkMonitorpointoroutputid() {
        return fkMonitorpointoroutputid;
    }

    public void setFkMonitorpointoroutputid(String fkMonitorpointoroutputid) {
        this.fkMonitorpointoroutputid = fkMonitorpointoroutputid;
    }

    public String getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(String fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode;
    }
    public Integer getIsshow() {
        return isshow;
    }

    public void setIsshow(Integer isshow) {
        this.isshow = isshow;
    }

    public String getRtsp() {
        return rtsp;
    }

    public void setRtsp(String rtsp) {
        this.rtsp = rtsp;
    }

    public Integer getVediocameratype() {
        return vediocameratype;
    }

    public void setVediocameratype(Integer vediocameratype) {
        this.vediocameratype = vediocameratype;
    }

    public String getFkVediocameracategory() {
        return fkVediocameracategory;
    }

    public void setFkVediocameracategory(String fkVediocameracategory) {
        this.fkVediocameracategory = fkVediocameracategory;
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getAlarmtime() {
        return alarmtime;
    }

    public void setAlarmtime(Date alarmtime) {
        this.alarmtime = alarmtime;
    }

    public String getPushurl() {
        return pushurl;
    }

    public void setPushurl(String pushurl) {
        this.pushurl = pushurl;
    }
}