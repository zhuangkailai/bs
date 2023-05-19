package com.tjpu.sp.model.environmentalprotection.monitorpoint;

import java.util.Date;

public class AirMonitorStationVO {
    private String pkAirid;

    private String fkRegioncode;

    private String monitorpointcode;

    private String monitorpointname;

    private String fkControllevelcode;

    private String fkAirfunclasscode;

    private Double longitude;

    private Double latitude;

    private String address;

    private Short pointtype;

    private Integer year;

    private Short status;

    private String dgimn;

    private Date updatetime;

    private String updateuser;

    public String getPkAirid() {
        return pkAirid;
    }

    public void setPkAirid(String pkAirid) {
        this.pkAirid = pkAirid;
    }

    public String getFkRegioncode() {
        return fkRegioncode;
    }

    public void setFkRegioncode(String fkRegioncode) {
        this.fkRegioncode = fkRegioncode;
    }

    public String getMonitorpointcode() {
        return monitorpointcode;
    }

    public void setMonitorpointcode(String monitorpointcode) {
        this.monitorpointcode = monitorpointcode;
    }

    public String getMonitorpointname() {
        return monitorpointname;
    }

    public void setMonitorpointname(String monitorpointname) {
        this.monitorpointname = monitorpointname;
    }

    public String getFkControllevelcode() {
        return fkControllevelcode;
    }

    public void setFkControllevelcode(String fkControllevelcode) {
        this.fkControllevelcode = fkControllevelcode;
    }

    public String getFkAirfunclasscode() {
        return fkAirfunclasscode;
    }

    public void setFkAirfunclasscode(String fkAirfunclasscode) {
        this.fkAirfunclasscode = fkAirfunclasscode;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Short getPointtype() {
        return pointtype;
    }

    public void setPointtype(Short pointtype) {
        this.pointtype = pointtype;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser;
    }
}