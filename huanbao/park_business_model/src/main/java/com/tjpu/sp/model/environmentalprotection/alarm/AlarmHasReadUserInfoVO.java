package com.tjpu.sp.model.environmentalprotection.alarm;

import java.util.Date;

public class AlarmHasReadUserInfoVO {
    private String pkId;

    private String fkPollutionid;

    private String fkMonitorpointid;

    private String fkMonitorpointtype;

    private Short remindtype;

    private Date monitortime;

    private String datatype;

    private String fkUserid;

    private Date userreadtime;

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

    public String getFkMonitorpointtype() {
        return fkMonitorpointtype;
    }

    public void setFkMonitorpointtype(String fkMonitorpointtype) {
        this.fkMonitorpointtype = fkMonitorpointtype == null ? null : fkMonitorpointtype.trim();
    }

    public Short getRemindtype() {
        return remindtype;
    }

    public void setRemindtype(Short remindtype) {
        this.remindtype = remindtype;
    }

    public Date getMonitortime() {
        return monitortime;
    }

    public void setMonitortime(Date monitortime) {
        this.monitortime = monitortime;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype == null ? null : datatype.trim();
    }

    public String getFkUserid() {
        return fkUserid;
    }

    public void setFkUserid(String fkUserid) {
        this.fkUserid = fkUserid == null ? null : fkUserid.trim();
    }

    public Date getUserreadtime() {
        return userreadtime;
    }

    public void setUserreadtime(Date userreadtime) {
        this.userreadtime = userreadtime;
    }
}