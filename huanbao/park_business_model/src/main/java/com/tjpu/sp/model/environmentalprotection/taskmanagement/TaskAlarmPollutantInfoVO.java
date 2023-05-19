package com.tjpu.sp.model.environmentalprotection.taskmanagement;

import java.util.Date;

public class TaskAlarmPollutantInfoVO {
    private String pkId;

    private String fkTaskid;

    private String fkPolluantcode;

    private String fkTasktype;

    private Date updatetime;

    private String alarmtype;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkTaskid() {
        return fkTaskid;
    }

    public void setFkTaskid(String fkTaskid) {
        this.fkTaskid = fkTaskid == null ? null : fkTaskid.trim();
    }

    public String getFkPolluantcode() {
        return fkPolluantcode;
    }

    public void setFkPolluantcode(String fkPolluantcode) {
        this.fkPolluantcode = fkPolluantcode == null ? null : fkPolluantcode.trim();
    }

    public String getFkTasktype() {
        return fkTasktype;
    }

    public void setFkTasktype(String fkTasktype) {
        this.fkTasktype = fkTasktype == null ? null : fkTasktype.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }


    public String getAlarmtype() {
        return alarmtype;
    }

    public void setAlarmtype(String alarmtype) {
        this.alarmtype = alarmtype;
    }
}