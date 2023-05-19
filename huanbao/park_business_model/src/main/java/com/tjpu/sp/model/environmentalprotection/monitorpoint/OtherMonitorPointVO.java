package com.tjpu.sp.model.environmentalprotection.monitorpoint;

import java.util.Date;

public class OtherMonitorPointVO {
    private String pkMonitorpointid;

    private String fkRegioncode;

    private String fkMonitorpointtypecode;

    private String monitorpointcode;

    private String monitorpointname;

    private Double longitude;

    private Double latitude;

    private String linkman;

    private String mobilephone;

    private String monitordepartment;

    private String fkFileid;

    private Short status;

    private Integer orderindex;

    private String remark;

    private String dgimn;

    private Date updatetime;

    private String updateuser;

    private String fkControllevelcode;

    private Integer monitorpointcategory;

    public String getPkMonitorpointid() {
        return pkMonitorpointid;
    }

    public void setPkMonitorpointid(String pkMonitorpointid) {
        this.pkMonitorpointid = pkMonitorpointid == null ? null : pkMonitorpointid.trim();
    }

    public String getFkRegioncode() {
        return fkRegioncode;
    }

    public void setFkRegioncode(String fkRegioncode) {
        this.fkRegioncode = fkRegioncode == null ? null : fkRegioncode.trim();
    }

    public String getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(String fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode == null ? null : fkMonitorpointtypecode.trim();
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

    public String getLinkman() {
        return linkman;
    }

    public void setLinkman(String linkman) {
        this.linkman = linkman == null ? null : linkman.trim();
    }

    public String getMobilephone() {
        return mobilephone;
    }

    public void setMobilephone(String mobilephone) {
        this.mobilephone = mobilephone == null ? null : mobilephone.trim();
    }

    public String getMonitordepartment() {
        return monitordepartment;
    }

    public void setMonitordepartment(String monitordepartment) {
        this.monitordepartment = monitordepartment == null ? null : monitordepartment.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
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
        this.remark = remark == null ? null : remark.trim();
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn == null ? null : dgimn.trim();
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
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public String getFkControllevelcode() {
        return fkControllevelcode;
    }

    public void setFkControllevelcode(String fkControllevelcode) {
        this.fkControllevelcode = fkControllevelcode == null ? null : fkControllevelcode.trim();
    }

    public Integer getMonitorpointcategory() {
        return monitorpointcategory;
    }

    public void setMonitorpointcategory(Integer monitorpointcategory) {
        this.monitorpointcategory = monitorpointcategory;
    }
}