package com.tjpu.sp.model.environmentalprotection.monitorpoint;

import java.util.Date;

public class AirVO {
    private String pkAirid;

    private String fkRegioncode;

    private String monitorpointcode;

    private String monitorpointname;

    private String fkControllevelcode;

    private String fkAirfunclasscode;

    private Double longitude;

    private Double latitude;

    private Date updatedate;

    private String fkDatasource;

    private String datasourcePkid;

    private String address;

    private Integer pointtype;

    private String year;

    private Integer isstop;

    public String getPkAirid() {
        return pkAirid;
    }

    public void setPkAirid(String pkAirid) {
        this.pkAirid = pkAirid == null ? null : pkAirid.trim();
    }

    public String getFkRegioncode() {
        return fkRegioncode;
    }

    public void setFkRegioncode(String fkRegioncode) {
        this.fkRegioncode = fkRegioncode == null ? null : fkRegioncode.trim();
    }

    public String getMonitorpointcode() {
        return monitorpointcode;
    }

    public void setMonitorpointcode(String monitorpointcode) {
        this.monitorpointcode = monitorpointcode == null ? null : monitorpointcode.trim();
    }

    public String getMonitorpointname() {
        return monitorpointname;
    }

    public void setMonitorpointname(String monitorpointname) {
        this.monitorpointname = monitorpointname == null ? null : monitorpointname.trim();
    }

    public String getFkControllevelcode() {
        return fkControllevelcode;
    }

    public void setFkControllevelcode(String fkControllevelcode) {
        this.fkControllevelcode = fkControllevelcode == null ? null : fkControllevelcode.trim();
    }

    public String getFkAirfunclasscode() {
        return fkAirfunclasscode;
    }

    public void setFkAirfunclasscode(String fkAirfunclasscode) {
        this.fkAirfunclasscode = fkAirfunclasscode == null ? null : fkAirfunclasscode.trim();
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

    public Date getUpdatedate() {
        return updatedate;
    }

    public void setUpdatedate(Date updatedate) {
        this.updatedate = updatedate;
    }

    public String getFkDatasource() {
        return fkDatasource;
    }

    public void setFkDatasource(String fkDatasource) {
        this.fkDatasource = fkDatasource == null ? null : fkDatasource.trim();
    }

    public String getDatasourcePkid() {
        return datasourcePkid;
    }

    public void setDatasourcePkid(String datasourcePkid) {
        this.datasourcePkid = datasourcePkid == null ? null : datasourcePkid.trim();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public Integer getPointtype() {
        return pointtype;
    }

    public void setPointtype(Integer pointtype) {
        this.pointtype = pointtype;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year == null ? null : year.trim();
    }

    public Integer getIsstop() {
        return isstop;
    }

    public void setIsstop(Integer isstop) {
        this.isstop = isstop;
    }
}