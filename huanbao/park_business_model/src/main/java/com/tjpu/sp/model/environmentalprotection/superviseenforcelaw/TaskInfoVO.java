package com.tjpu.sp.model.environmentalprotection.superviseenforcelaw;

public class TaskInfoVO {
    private String pkTaskid;

    private String fkPollutionid;

    private String taskname;

    private String taskid;

    private String publishtime;

    private String endtime;

    private String taskstatus;

    private String executepersion;

    private String completetime;

    private String fkTasksourcecode;

    private String fkTasktypecode;

    private String taskremark;

    private String fkEnerlvlcode;

    private String opinion;

    private String belong;

    private String fkFileid;

    private String remark;

    private String updateuser;

    private String updatetime;

    public String getPkTaskid() {
        return pkTaskid;
    }

    public void setPkTaskid(String pkTaskid) {
        this.pkTaskid = pkTaskid == null ? null : pkTaskid.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getTaskname() {
        return taskname;
    }

    public void setTaskname(String taskname) {
        this.taskname = taskname == null ? null : taskname.trim();
    }

    public String getTaskid() {
        return taskid;
    }

    public void setTaskid(String taskid) {
        this.taskid = taskid == null ? null : taskid.trim();
    }

    public String getPublishtime() {
        return  "".equals(publishtime)?null:publishtime;
    }

    public void setPublishtime(String publishtime) {
        this.publishtime = publishtime;
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime == null ? null : endtime.trim();
    }

    public String getTaskstatus() {
        return taskstatus;
    }

    public void setTaskstatus(String taskstatus) {
        this.taskstatus = taskstatus == null ? null : taskstatus.trim();
    }

    public String getExecutepersion() {
        return executepersion;
    }

    public void setExecutepersion(String executepersion) {
        this.executepersion = executepersion == null ? null : executepersion.trim();
    }

    public String getCompletetime() {
        return "".equals(completetime)?null:completetime;
    }

    public void setCompletetime(String completetime) {
        this.completetime = completetime;
    }

    public String getFkTasksourcecode() {
        return fkTasksourcecode;
    }

    public void setFkTasksourcecode(String fkTasksourcecode) {
        this.fkTasksourcecode = fkTasksourcecode == null ? null : fkTasksourcecode.trim();
    }

    public String getFkTasktypecode() {
        return fkTasktypecode;
    }

    public void setFkTasktypecode(String fkTasktypecode) {
        this.fkTasktypecode = fkTasktypecode == null ? null : fkTasktypecode.trim();
    }

    public String getTaskremark() {
        return taskremark;
    }

    public void setTaskremark(String taskremark) {
        this.taskremark = taskremark == null ? null : taskremark.trim();
    }

    public String getFkEnerlvlcode() {
        return fkEnerlvlcode;
    }

    public void setFkEnerlvlcode(String fkEnerlvlcode) {
        this.fkEnerlvlcode = fkEnerlvlcode == null ? null : fkEnerlvlcode.trim();
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion == null ? null : opinion.trim();
    }

    public String getBelong() {
        return belong;
    }

    public void setBelong(String belong) {
        this.belong = belong == null ? null : belong.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getUpdatetime() {
        return "".equals(updatetime)?null:updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser;
    }
}