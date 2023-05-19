package com.tjpu.sp.model.environmentalprotection.licence;

import java.util.Date;

public class RPUnGasPollutantConcentrationVO {
    private String pkId;

    private String fkReportid;

    private String fkOutputid;

    private Date monitortime;

    private String fkPollutantcode;

    private Double limitvalue;

    private String monitorfacility;

    private Double monitorvalue;

    private Integer isover;

    private String overreason;

    private String remark;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkReportid() {
        return fkReportid;
    }

    public void setFkReportid(String fkReportid) {
        this.fkReportid = fkReportid == null ? null : fkReportid.trim();
    }

    public String getFkOutputid() {
        return fkOutputid;
    }

    public void setFkOutputid(String fkOutputid) {
        this.fkOutputid = fkOutputid == null ? null : fkOutputid.trim();
    }

    public Date getMonitortime() {
        return monitortime;
    }

    public void setMonitortime(Date monitortime) {
        this.monitortime = monitortime;
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public Double getLimitvalue() {
        return limitvalue;
    }

    public void setLimitvalue(Double limitvalue) {
        this.limitvalue = limitvalue;
    }

    public String getMonitorfacility() {
        return monitorfacility;
    }

    public void setMonitorfacility(String monitorfacility) {
        this.monitorfacility = monitorfacility == null ? null : monitorfacility.trim();
    }

    public Double getMonitorvalue() {
        return monitorvalue;
    }

    public void setMonitorvalue(Double monitorvalue) {
        this.monitorvalue = monitorvalue;
    }

    public Integer getIsover() {
        return isover;
    }

    public void setIsover(Integer isover) {
        this.isover = isover;
    }

    public String getOverreason() {
        return overreason;
    }

    public void setOverreason(String overreason) {
        this.overreason = overreason == null ? null : overreason.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
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
}