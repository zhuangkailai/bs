package com.tjpu.sp.model.environmentalprotection.monitorcontrol;

import java.util.Date;

public class MonitorPointMonitorControlVO {
    private String pkId;

    private String fkMonitorpointid;

    private String fkMonitorpointtypecode;

    private Date startmointortime;

    private Date stopmointortime;

    private Date beforestartmointortime;

    private Date beforestopmointortime;

    private String startpeople;

    private String stoppeople;

    private String dgimn;

    private Date updatetime;

    private String updateuser;

    private String fkFileid;

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
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

    public Date getStartmointortime() {
        return startmointortime;
    }

    public void setStartmointortime(Date startmointortime) {
        this.startmointortime = startmointortime;
    }

    public Date getStopmointortime() {
        return stopmointortime;
    }

    public void setStopmointortime(Date stopmointortime) {
        this.stopmointortime = stopmointortime;
    }

    public Date getBeforestartmointortime() {
        return beforestartmointortime;
    }

    public void setBeforestartmointortime(Date beforestartmointortime) {
        this.beforestartmointortime = beforestartmointortime;
    }

    public Date getBeforestopmointortime() {
        return beforestopmointortime;
    }

    public void setBeforestopmointortime(Date beforestopmointortime) {
        this.beforestopmointortime = beforestopmointortime;
    }

    public String getStartpeople() {
        return startpeople;
    }

    public void setStartpeople(String startpeople) {
        this.startpeople = startpeople == null ? null : startpeople.trim();
    }

    public String getStoppeople() {
        return stoppeople;
    }

    public void setStoppeople(String stoppeople) {
        this.stoppeople = stoppeople == null ? null : stoppeople.trim();
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn;
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