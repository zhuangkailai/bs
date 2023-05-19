package com.tjpu.sp.model.environmentalprotection.licence;

import java.util.Date;

public class RPGasPollutantSpeedVO {
    private String pkId;

    private String fkReportid;

    private String fkOutputid;

    private String fkPollutantcode;

    private Integer effectivenum;

    private Double limitspeedvalue;

    private Double minvalue;

    private Double maxvalue;

    private Double avgvalue;

    private Integer overnum;

    private Double overrate;

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

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public Integer getEffectivenum() {
        return effectivenum;
    }

    public void setEffectivenum(Integer effectivenum) {
        this.effectivenum = effectivenum;
    }

    public Double getLimitspeedvalue() {
        return limitspeedvalue;
    }

    public void setLimitspeedvalue(Double limitspeedvalue) {
        this.limitspeedvalue = limitspeedvalue;
    }

    public Double getMinvalue() {
        return minvalue;
    }

    public void setMinvalue(Double minvalue) {
        this.minvalue = minvalue;
    }

    public Double getMaxvalue() {
        return maxvalue;
    }

    public void setMaxvalue(Double maxvalue) {
        this.maxvalue = maxvalue;
    }

    public Double getAvgvalue() {
        return avgvalue;
    }

    public void setAvgvalue(Double avgvalue) {
        this.avgvalue = avgvalue;
    }

    public Integer getOvernum() {
        return overnum;
    }

    public void setOvernum(Integer overnum) {
        this.overnum = overnum;
    }

    public Double getOverrate() {
        return overrate;
    }

    public void setOverrate(Double overrate) {
        this.overrate = overrate;
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