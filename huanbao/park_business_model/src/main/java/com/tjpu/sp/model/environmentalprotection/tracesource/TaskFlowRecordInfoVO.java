package com.tjpu.sp.model.environmentalprotection.tracesource;

import java.util.Date;

public class TaskFlowRecordInfoVO {
    private String pkId;

    private String fkTaskid;

    private String fkTasktype;

    private String currenttaskstatus;

    private String nexttaskstatus;

    private String fkTaskhandleuserid;

    private Date taskhandletime;

    private String taskhandleopinion;

    private String taskcomment;

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

    public String getFkTasktype() {
        return fkTasktype;
    }

    public void setFkTasktype(String fkTasktype) {
        this.fkTasktype = fkTasktype == null ? null : fkTasktype.trim();
    }

    public String getCurrenttaskstatus() {
        return currenttaskstatus;
    }

    public void setCurrenttaskstatus(String currenttaskstatus) {
        this.currenttaskstatus = currenttaskstatus == null ? null : currenttaskstatus.trim();
    }

    public String getNexttaskstatus() {
        return nexttaskstatus;
    }

    public void setNexttaskstatus(String nexttaskstatus) {
        this.nexttaskstatus = nexttaskstatus == null ? null : nexttaskstatus.trim();
    }

    public String getFkTaskhandleuserid() {
        return fkTaskhandleuserid;
    }

    public void setFkTaskhandleuserid(String fkTaskhandleuserid) {
        this.fkTaskhandleuserid = fkTaskhandleuserid == null ? null : fkTaskhandleuserid.trim();
    }

    public Date getTaskhandletime() {
        return taskhandletime;
    }

    public void setTaskhandletime(Date taskhandletime) {
        this.taskhandletime = taskhandletime;
    }

    public String getTaskhandleopinion() {
        return taskhandleopinion;
    }

    public void setTaskhandleopinion(String taskhandleopinion) {
        this.taskhandleopinion = taskhandleopinion == null ? null : taskhandleopinion.trim();
    }

    public String getTaskcomment() {
        return taskcomment;
    }

    public void setTaskcomment(String taskcomment) {
        this.taskcomment = taskcomment;
    }
}