package com.tjpu.sp.model.environmentalprotection.licence;

import java.util.Date;

public class RPExceptionUnGasPollutantConcentrationVO {
    private String pkId;

    private String fkReportid;

    private Date starttime;

    private Date endtime;

    private String fkOutputid;

    private Date monitortime;

    private String fkPollutantcode;

    private Integer monitornum;

    private Double limitvalue;

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

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
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

    public Integer getMonitornum() {
        return monitornum;
    }

    public void setMonitornum(Integer monitornum) {
        this.monitornum = monitornum;
    }

    public Double getLimitvalue() {
        return limitvalue;
    }

    public void setLimitvalue(Double limitvalue) {
        this.limitvalue = limitvalue;
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