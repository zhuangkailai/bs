package com.tjpu.sp.model.environmentalprotection.monitorpoint;

public class WaterStationVO {
    private String pkWaterstationid;

    private String fkWaterbodycode;

    private String monitorpointcode;

    private String monitorpointname;

    private String fkControllevelcode;

    private String fkWaterbodytypecode;

    private Double longitude;

    private Double latitude;

    private String fkFunwaterqaulitycode;

    private String stationtyear;

    private String fkFileid;

    private Short status;

    private Integer orderindex;

    private String remark;

    private String dgimn;

    public String getPkWaterstationid() {
        return pkWaterstationid;
    }

    public void setPkWaterstationid(String pkWaterstationid) {
        this.pkWaterstationid = pkWaterstationid;
    }

    public String getFkWaterbodycode() {
        return fkWaterbodycode;
    }

    public void setFkWaterbodycode(String fkWaterbodycode) {
        this.fkWaterbodycode = fkWaterbodycode;
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

    public String getFkWaterbodytypecode() {
        return fkWaterbodytypecode;
    }

    public void setFkWaterbodytypecode(String fkWaterbodytypecode) {
        this.fkWaterbodytypecode = fkWaterbodytypecode;
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

    public String getFkFunwaterqaulitycode() {
        return fkFunwaterqaulitycode;
    }

    public void setFkFunwaterqaulitycode(String fkFunwaterqaulitycode) {
        this.fkFunwaterqaulitycode = fkFunwaterqaulitycode;
    }

    public String getStationtyear() {
        return stationtyear;
    }

    public void setStationtyear(String stationtyear) {
        this.stationtyear = stationtyear;
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn;
    }
}