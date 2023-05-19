package com.tjpu.sp.model.environmentalprotection.cjpz;

import java.util.Date;

public class PointAddressSetVO {
    private String pkId;

    private String fkEntconnectid;

    private String fkMonitorpointtypecode;

    private String dgimn;

    private String fkPollutantcode;

    private Integer slaveid;

    private String address;

    private String convertway;

    private String remark;

    private String updateuser;

    private Date updatetime;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkEntconnectid() {
        return fkEntconnectid;
    }

    public void setFkEntconnectid(String fkEntconnectid) {
        this.fkEntconnectid = fkEntconnectid == null ? null : fkEntconnectid.trim();
    }

    public String getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(String fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode == null ? null : fkMonitorpointtypecode.trim();
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn == null ? null : dgimn.trim();
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public Integer getSlaveid() {
        return slaveid;
    }

    public void setSlaveid(Integer slaveid) {
        this.slaveid = slaveid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getConvertway() {
        return convertway;
    }

    public void setConvertway(String convertway) {
        this.convertway = convertway == null ? null : convertway.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
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