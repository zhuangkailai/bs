package com.tjpu.sp.model.environmentalprotection.tracesource;

import java.util.List;
import java.util.Map;

public class TraceSourceEventInfoVO {
    private String pkId;

    private String fkPetitionid;

    private String eventname;

    private Double longitude;

    private Double latitude;

    private String starttime;

    private String endtime;

    private Integer duration;

    private Short eventtype;

    private Short eventstatus;

    private String tracesourceexplain;

    private String eventmark;

    private String updatetime;

    private String updateuser;
    private String voyagestarttime;
    private String voyageendtime;
    private String voyagejson;

    private String consultationresult;
    private String eventtypename;
    private String status;
    private List<Map<String,Object>> eventDetail;
    private List<String> monitorData;
    private List<Map> consultationData;

    public String getVoyagejson() {
        return voyagejson;
    }

    public void setVoyagejson(String voyagejson) {
        this.voyagejson = voyagejson;
    }

    public String getVoyagestarttime() {
        return voyagestarttime;
    }

    public void setVoyagestarttime(String voyagestarttime) {
        this.voyagestarttime = voyagestarttime;
    }

    public String getVoyageendtime() {
        return voyageendtime;
    }

    public void setVoyageendtime(String voyageendtime) {
        this.voyageendtime = voyageendtime;
    }

    public List<Map> getConsultationData() {
        return consultationData;
    }

    public void setConsultationData(List<Map> consultationData) {
        this.consultationData = consultationData;
    }

    public List<String> getMonitorData() {
        return monitorData;
    }

    public void setMonitorData(List<String> monitorData) {
        this.monitorData = monitorData;
    }

    public String getEventtypename() {
        return eventtypename;
    }

    public void setEventtypename(String eventtypename) {
        this.eventtypename = eventtypename;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Map<String, Object>> getEventDetail() {
        return eventDetail;
    }

    public void setEventDetail(List<Map<String, Object>> eventDetail) {
        this.eventDetail = eventDetail;
    }

    public String getConsultationresult() {
        return consultationresult;
    }

    public void setConsultationresult(String consultationresult) {
        this.consultationresult = consultationresult;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public String getFkPetitionid() {
        return fkPetitionid;
    }

    public void setFkPetitionid(String fkPetitionid) {
        this.fkPetitionid = fkPetitionid;
    }

    public String getEventname() {
        return eventname;
    }

    public void setEventname(String eventname) {
        this.eventname = eventname;
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

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Short getEventtype() {
        return eventtype;
    }

    public void setEventtype(Short eventtype) {
        this.eventtype = eventtype;
    }

    public Short getEventstatus() {
        return eventstatus;
    }

    public void setEventstatus(Short eventstatus) {
        this.eventstatus = eventstatus;
    }

    public String getTracesourceexplain() {
        return tracesourceexplain;
    }

    public void setTracesourceexplain(String tracesourceexplain) {
        this.tracesourceexplain = tracesourceexplain;
    }

    public String getEventmark() {
        return eventmark;
    }

    public void setEventmark(String eventmark) {
        this.eventmark = eventmark;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser;
    }
}