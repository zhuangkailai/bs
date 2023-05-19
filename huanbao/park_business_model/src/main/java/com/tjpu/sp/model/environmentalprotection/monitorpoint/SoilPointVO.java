package com.tjpu.sp.model.environmentalprotection.monitorpoint;

public class SoilPointVO {
    private String pkId;

    private String fkSoilpointtypecode;

    private String fkPollutionid;

    private String monitorpointcode;

    private String monitorpointname;

    private String dgimn;

    private String fkControllevelcode;

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

    public String getFkSoilpointtypecode() {
        return fkSoilpointtypecode;
    }

    public void setFkSoilpointtypecode(String fkSoilpointtypecode) {
        this.fkSoilpointtypecode = fkSoilpointtypecode == null ? null : fkSoilpointtypecode.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
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

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn == null ? null : dgimn.trim();
    }

    public String getFkControllevelcode() {
        return fkControllevelcode;
    }

    public void setFkControllevelcode(String fkControllevelcode) {
        this.fkControllevelcode = fkControllevelcode == null ? null : fkControllevelcode.trim();
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