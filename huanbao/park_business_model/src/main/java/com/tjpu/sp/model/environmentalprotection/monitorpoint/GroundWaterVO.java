package com.tjpu.sp.model.environmentalprotection.monitorpoint;

public class GroundWaterVO {
    private String pkId;

    private String fkWaterbodycode;

    private String monitorpointcode;

    private String monitorpointname;

    private String fkControllevelcode;

    private String fkWaterbodytypecode;

    private String fkFunwaterqaulitycode;

    private String dgimn;

    private Double longitude;

    private Double latitude;

    private Short status;

    private Integer stationtyear;

    private Integer orderindex;

    private String fkImgid;

    private String fkFileid;

    private String remark;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkWaterbodycode() {
        return fkWaterbodycode;
    }

    public void setFkWaterbodycode(String fkWaterbodycode) {
        this.fkWaterbodycode = fkWaterbodycode == null ? null : fkWaterbodycode.trim();
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

    public String getFkWaterbodytypecode() {
        return fkWaterbodytypecode;
    }

    public void setFkWaterbodytypecode(String fkWaterbodytypecode) {
        this.fkWaterbodytypecode = fkWaterbodytypecode == null ? null : fkWaterbodytypecode.trim();
    }

    public String getFkFunwaterqaulitycode() {
        return fkFunwaterqaulitycode;
    }

    public void setFkFunwaterqaulitycode(String fkFunwaterqaulitycode) {
        this.fkFunwaterqaulitycode = fkFunwaterqaulitycode == null ? null : fkFunwaterqaulitycode.trim();
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn == null ? null : dgimn.trim();
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

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public Integer getStationtyear() {
        return stationtyear;
    }

    public void setStationtyear(Integer stationtyear) {
        this.stationtyear = stationtyear;
    }

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
    }

    public String getFkImgid() {
        return fkImgid;
    }

    public void setFkImgid(String fkImgid) {
        this.fkImgid = fkImgid == null ? null : fkImgid.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }
}