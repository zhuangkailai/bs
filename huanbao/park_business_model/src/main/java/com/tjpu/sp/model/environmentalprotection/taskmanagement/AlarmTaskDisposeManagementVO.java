package com.tjpu.sp.model.environmentalprotection.taskmanagement;

import java.util.Date;
import java.util.Set;

public class AlarmTaskDisposeManagementVO {
    private String pkTaskid;

    private String fkPollutionid;

    private String fkTasktype;

    private String taskname;

    private String taskcreatetime;

    private Integer taskstatus;

    private String taskremark;

    private String fkProblemtype;

    private String feedbackresults;

    private Short issampling;

    private String fileid;

    private Date updatetime;

    private String updateuser;

    private Set zhixingren;

    private String fenpairen;

    private String fenpaidate;

    private String fkTasksource;

    private String taskstartdate;

    private String taskenddate;

    private String taskrealstartdate;

    private String taskrealenddate;

    private String fkmonitorpointtypecode;
    private String disposer;
    private String disposaltime;
    private String reportingtime;

    private String taskendtime;

    private String alarmstarttime;

    private String overlevelcode;

    private Short recoverystatus;

    private Date recoverytime;

    public String getDisposer() {
        return disposer;
    }

    public void setDisposer(String disposer) {
        this.disposer = disposer;
    }

    public String getDisposaltime() {
        return disposaltime;
    }

    public void setDisposaltime(String disposaltime) {
        this.disposaltime = disposaltime;
    }

    public String getReportingtime() {
        return reportingtime;
    }

    public void setReportingtime(String reportingtime) {
        this.reportingtime = reportingtime;
    }

    public String getFkTasksource() {
        return fkTasksource;
    }

    public void setFkTasksource(String fkTasksource) {
        this.fkTasksource = fkTasksource;
    }


    public Set getZhixingren() {
        return zhixingren;
    }

    public void setZhixingren(Set zhixingren) {
        this.zhixingren = zhixingren;
    }

    public String getFenpairen() {
        return fenpairen;
    }

    public void setFenpairen(String fenpairen) {
        this.fenpairen = fenpairen;
    }

    public String getFenpaidate() {
        return fenpaidate;
    }

    public void setFenpaidate(String fenpaidate) {
        this.fenpaidate = fenpaidate;
    }

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

    public String getFkTasktype() {
        return fkTasktype;
    }

    public void setFkTasktype(String fkTasktype) {
        this.fkTasktype = fkTasktype == null ? null : fkTasktype.trim();
    }

    public String getTaskname() {
        return taskname;
    }

    public void setTaskname(String taskname) {
        this.taskname = taskname == null ? null : taskname.trim();
    }

    public String getTaskcreatetime() {
        return taskcreatetime;
    }

    public void setTaskcreatetime(String taskcreatetime) {
        this.taskcreatetime = taskcreatetime;
    }

    public String getTaskstartdate() {
        return taskstartdate;
    }

    public void setTaskstartdate(String taskstartdate) {
        this.taskstartdate = taskstartdate==null?null:taskstartdate;
    }

    public String getTaskenddate() {
        return taskenddate;
    }

    public void setTaskenddate(String taskenddate) {
        this.taskenddate = taskenddate==null?null:taskenddate;
    }

    public String getTaskrealstartdate() {
        return taskrealstartdate;
    }

    public void setTaskrealstartdate(String taskrealstartdate) {
        this.taskrealstartdate = taskrealstartdate;
    }

    public String getTaskrealenddate() {
        return taskrealenddate;
    }

    public void setTaskrealenddate(String taskrealenddate) {
        this.taskrealenddate = taskrealenddate;
    }

    public String getTaskremark() {
        return taskremark;
    }

    public void setTaskremark(String taskremark) {
        this.taskremark = taskremark == null ? null : taskremark.trim();
    }

    public String getFkProblemtype() {
        return fkProblemtype;
    }

    public void setFkProblemtype(String fkProblemtype) {
        this.fkProblemtype = fkProblemtype == null ? null : fkProblemtype.trim();
    }

    public String getFeedbackresults() {
        return feedbackresults;
    }

    public void setFeedbackresults(String feedbackresults) {
        this.feedbackresults = feedbackresults == null ? null : feedbackresults.trim();
    }

    public Short getIssampling() {
        return issampling;
    }

    public void setIssampling(Short issampling) {
        this.issampling = issampling;
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

    public Integer getTaskstatus() {
        return taskstatus;
    }

    public void setTaskstatus(Integer taskstatus) {
        this.taskstatus = taskstatus;
    }

    public String getFkmonitorpointtypecode() {
        return fkmonitorpointtypecode;
    }

    public void setFkmonitorpointtypecode(String fkmonitorpointtypecode) {
        this.fkmonitorpointtypecode = fkmonitorpointtypecode;
    }

    public String getTaskendtime() {
        return taskendtime;
    }

    public void setTaskendtime(String taskendtime) {
        this.taskendtime = taskendtime;
    }

    public String getAlarmstarttime() {
        return alarmstarttime;
    }

    public void setAlarmstarttime(String alarmstarttime) {
        this.alarmstarttime = alarmstarttime;
    }

    public String getOverlevelcode() {
        return overlevelcode;
    }

    public void setOverlevelcode(String overlevelcode) {
        this.overlevelcode = overlevelcode;
    }

    public Short getRecoverystatus() {
        return recoverystatus;
    }

    public void setRecoverystatus(Short recoverystatus) {
        this.recoverystatus = recoverystatus;
    }

    public Date getRecoverytime() {
        return recoverytime;
    }

    public void setRecoverytime(Date recoverytime) {
        this.recoverytime = recoverytime;
    }
}