package com.tjpu.sp.model.environmentalprotection.devopsinfo;

import java.util.Date;

public class EntDevOpsInfoVO {
    private String pkId;

    private String fkPollutionid;

    private String fkMonitorpointid;

    private String fkMonitorpointtypecode;

    private String devopsunit;

    private String devopspeople;
    private String telephone;

    private Date updatetime;

    private String updateuser;

    private Date devopsstartdate;
    private Date devopsenddate;

    private Short weekpatrol;
    private Short monthpatrol;
    private Short twomonthpatrol;
    private Short quarterpatrol;
    private Short halfyearpatrol;
    private Short yearpatrol;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getFkMonitorpointid() {
        return fkMonitorpointid;
    }

    public void setFkMonitorpointid(String fkMonitorpointid) {
        this.fkMonitorpointid = fkMonitorpointid == null ? null : fkMonitorpointid.trim();
    }

    public String getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(String fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode == null ? null : fkMonitorpointtypecode.trim();
    }

    public String getDevopsunit() {
        return devopsunit;
    }

    public void setDevopsunit(String devopsunit) {
        this.devopsunit = devopsunit == null ? null : devopsunit.trim();
    }

    public String getDevopspeople() {
        return devopspeople;
    }

    public void setDevopspeople(String devopspeople) {
        this.devopspeople = devopspeople == null ? null : devopspeople.trim();
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

    public Date getDevopsstartdate() {
        return devopsstartdate;
    }

    public void setDevopsstartdate(Date devopsstartdate) {
        this.devopsstartdate = devopsstartdate;
    }

    public Date getDevopsenddate() {
        return devopsenddate;
    }

    public void setDevopsenddate(Date devopsenddate) {
        this.devopsenddate = devopsenddate;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Short getWeekpatrol() {
        return weekpatrol;
    }

    public void setWeekpatrol(Short weekpatrol) {
        this.weekpatrol = weekpatrol;
    }

    public Short getMonthpatrol() {
        return monthpatrol;
    }

    public void setMonthpatrol(Short monthpatrol) {
        this.monthpatrol = monthpatrol;
    }

    public Short getTwomonthpatrol() {
        return twomonthpatrol;
    }

    public void setTwomonthpatrol(Short twomonthpatrol) {
        this.twomonthpatrol = twomonthpatrol;
    }

    public Short getQuarterpatrol() {
        return quarterpatrol;
    }

    public void setQuarterpatrol(Short quarterpatrol) {
        this.quarterpatrol = quarterpatrol;
    }

    public Short getHalfyearpatrol() {
        return halfyearpatrol;
    }

    public void setHalfyearpatrol(Short halfyearpatrol) {
        this.halfyearpatrol = halfyearpatrol;
    }

    public Short getYearpatrol() {
        return yearpatrol;
    }

    public void setYearpatrol(Short yearpatrol) {
        this.yearpatrol = yearpatrol;
    }
}