package com.tjpu.sp.model.environmentalprotection.monitorpoint;

import java.util.Date;

public class RiverSectionVO {
    private String pkId;

    private String rivercode;

    private String rivername;

    private String sectioncode;

    private String sectionname;

    private String fkSectiontypecode;

    private Double longitude;

    private Double latitude;

    private String location;

    private String fkFunwaterlevelcode;

    private String fkWatersystemcode;

    private String trunktreamcode;

    private String trunktreamname;

    private String updateuser;

    private Date updatetime;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getRivercode() {
        return rivercode;
    }

    public void setRivercode(String rivercode) {
        this.rivercode = rivercode == null ? null : rivercode.trim();
    }

    public String getRivername() {
        return rivername;
    }

    public void setRivername(String rivername) {
        this.rivername = rivername == null ? null : rivername.trim();
    }

    public String getSectioncode() {
        return sectioncode;
    }

    public void setSectioncode(String sectioncode) {
        this.sectioncode = sectioncode == null ? null : sectioncode.trim();
    }

    public String getSectionname() {
        return sectionname;
    }

    public void setSectionname(String sectionname) {
        this.sectionname = sectionname == null ? null : sectionname.trim();
    }

    public String getFkSectiontypecode() {
        return fkSectiontypecode;
    }

    public void setFkSectiontypecode(String fkSectiontypecode) {
        this.fkSectiontypecode = fkSectiontypecode == null ? null : fkSectiontypecode.trim();
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location == null ? null : location.trim();
    }

    public String getFkFunwaterlevelcode() {
        return fkFunwaterlevelcode;
    }

    public void setFkFunwaterlevelcode(String fkFunwaterlevelcode) {
        this.fkFunwaterlevelcode = fkFunwaterlevelcode == null ? null : fkFunwaterlevelcode.trim();
    }

    public String getFkWatersystemcode() {
        return fkWatersystemcode;
    }

    public void setFkWatersystemcode(String fkWatersystemcode) {
        this.fkWatersystemcode = fkWatersystemcode == null ? null : fkWatersystemcode.trim();
    }

    public String getTrunktreamcode() {
        return trunktreamcode;
    }

    public void setTrunktreamcode(String trunktreamcode) {
        this.trunktreamcode = trunktreamcode == null ? null : trunktreamcode.trim();
    }

    public String getTrunktreamname() {
        return trunktreamname;
    }

    public void setTrunktreamname(String trunktreamname) {
        this.trunktreamname = trunktreamname == null ? null : trunktreamname.trim();
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}