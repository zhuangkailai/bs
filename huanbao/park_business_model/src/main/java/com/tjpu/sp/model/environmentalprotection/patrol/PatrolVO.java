package com.tjpu.sp.model.environmentalprotection.patrol;

import java.util.Date;

public class PatrolVO {
    private String pkId;

    private String fkPollutionid;

    private String fkPatroltypecode;

    private String patrolpeople;

    private Date patroltime;

    private String patrolcontent;

    private Short ishasproblem;

    private String problemremark;

    private Short status;

    private String fkMonitorpointid;

    private String fkMonitorpointtypecode;

    private String fileid;

    private Date updatetime;

    private String updateuser;

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

    public String getFkPatroltypecode() {
        return fkPatroltypecode;
    }

    public void setFkPatroltypecode(String fkPatroltypecode) {
        this.fkPatroltypecode = fkPatroltypecode == null ? null : fkPatroltypecode.trim();
    }

    public String getPatrolpeople() {
        return patrolpeople;
    }

    public void setPatrolpeople(String patrolpeople) {
        this.patrolpeople = patrolpeople == null ? null : patrolpeople.trim();
    }

    public Date getPatroltime() {
        return patroltime;
    }

    public void setPatroltime(Date patroltime) {
        this.patroltime = patroltime;
    }

    public String getPatrolcontent() {
        return patrolcontent;
    }

    public void setPatrolcontent(String patrolcontent) {
        this.patrolcontent = patrolcontent == null ? null : patrolcontent.trim();
    }

    public Short getIshasproblem() {
        return ishasproblem;
    }

    public void setIshasproblem(Short ishasproblem) {
        this.ishasproblem = ishasproblem;
    }

    public String getProblemremark() {
        return problemremark;
    }

    public void setProblemremark(String problemremark) {
        this.problemremark = problemremark == null ? null : problemremark.trim();
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
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

    public String getFileid() {
        return fileid;
    }

    public void setFileid(String fileid) {
        this.fileid = fileid == null ? null : fileid.trim();
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