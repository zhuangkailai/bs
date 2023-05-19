
package com.tjpu.sp.model.environmentalprotection.tracesourcesample;


public class TraceSourceSampleVO {

    private String pkid;

    private String samplename;

    private String fkmonitorpointid;

    private String fkmonitorpointtypecode;

    private String sampletime;

    private Double longitude;

    private Double latitude;

    private String remark;

    private String updateuser;
    private String characterpollutants;

    private String updatetime;
    private Integer isfingerdatabase;
    private String fkpollutionid;

    public String getCharacterpollutants() {
        return characterpollutants;
    }

    public void setCharacterpollutants(String characterpollutants) {
        this.characterpollutants = characterpollutants;
    }

    public Integer getIsfingerdatabase() {
        return isfingerdatabase;
    }

    public void setIsfingerdatabase(Integer isfingerdatabase) {
        this.isfingerdatabase = isfingerdatabase;
    }

    public String getFkpollutionid() {
        return fkpollutionid;
    }

    public void setFkpollutionid(String fkpollutionid) {
        this.fkpollutionid = fkpollutionid;
    }

    public String getFkmonitorpointtypecode() {
        return fkmonitorpointtypecode;
    }

    public void setFkmonitorpointtypecode(String fkmonitorpointtypecode) {
        this.fkmonitorpointtypecode = fkmonitorpointtypecode;
    }

    public void setpkid(String pkid) {
        this.pkid = pkid;
    }


    public String getpkid() {
        return "".equals(pkid)?null:pkid;
        
    }
    public void setsamplename(String samplename) {
        this.samplename = samplename;
    }


    public String getsamplename() {
        return "".equals(samplename)?null:samplename;
        
    }
    public void setfkmonitorpointid(String fkmonitorpointid) {
        this.fkmonitorpointid = fkmonitorpointid;
    }


    public String getfkmonitorpointid() {
        return "".equals(fkmonitorpointid)?null:fkmonitorpointid;
        
    }
    public void setsampletime(String sampletime) {
        this.sampletime = sampletime;
    }


    public String getsampletime() {
        return "".equals(sampletime)?null:sampletime;
        
    }
    public void setlongitude(Double longitude) {
        this.longitude = longitude;
    }


    public Double getlongitude() {
        return longitude;
    }
    public void setlatitude(Double latitude) {
        this.latitude = latitude;
    }


    public Double getlatitude() {
        return latitude;
    }
    public void setremark(String remark) {
        this.remark = remark;
    }


    public String getremark() {
        return "".equals(remark)?null:remark;
        
    }
    public void setupdateuser(String updateuser) {
        this.updateuser = updateuser;
    }


    public String getupdateuser() {
        return "".equals(updateuser)?null:updateuser;
        
    }
    public void setupdatetime(String updatetime) {
        this.updatetime = updatetime;
    }


    public String getupdatetime() {
        return "".equals(updatetime)?null:updatetime;
        
    }

}
