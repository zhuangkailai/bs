package com.tjpu.sp.model.envhousekeepers.treatmentrunrecord;

import java.util.Date;

public class WaterTreatmentRunRecordVO {
    private String pkId;

    private String fkPollutionid;

    private String treatmentname;

    private String treatmentnum;

    private Date exceptionstarttime;

    private Date exceptionendtime;

    private String fkPollutantcode;

    private Double flowquantity;

    private String fkDraindirection;

    private String eventcause;

    private Short isreport;

    private String solutions;

    private String recorduser;

    private Date recordtime;

    private String revieweruser;

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

    public String getTreatmentname() {
        return treatmentname;
    }

    public void setTreatmentname(String treatmentname) {
        this.treatmentname = treatmentname == null ? null : treatmentname.trim();
    }

    public String getTreatmentnum() {
        return treatmentnum;
    }

    public void setTreatmentnum(String treatmentnum) {
        this.treatmentnum = treatmentnum == null ? null : treatmentnum.trim();
    }

    public Date getExceptionstarttime() {
        return exceptionstarttime;
    }

    public void setExceptionstarttime(Date exceptionstarttime) {
        this.exceptionstarttime = exceptionstarttime;
    }

    public Date getExceptionendtime() {
        return exceptionendtime;
    }

    public void setExceptionendtime(Date exceptionendtime) {
        this.exceptionendtime = exceptionendtime;
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public Double getFlowquantity() {
        return flowquantity;
    }

    public void setFlowquantity(Double flowquantity) {
        this.flowquantity = flowquantity;
    }

    public String getFkDraindirection() {
        return fkDraindirection;
    }

    public void setFkDraindirection(String fkDraindirection) {
        this.fkDraindirection = fkDraindirection == null ? null : fkDraindirection.trim();
    }

    public String getEventcause() {
        return eventcause;
    }

    public void setEventcause(String eventcause) {
        this.eventcause = eventcause == null ? null : eventcause.trim();
    }

    public Short getIsreport() {
        return isreport;
    }

    public void setIsreport(Short isreport) {
        this.isreport = isreport;
    }

    public String getSolutions() {
        return solutions;
    }

    public void setSolutions(String solutions) {
        this.solutions = solutions == null ? null : solutions.trim();
    }

    public String getRecorduser() {
        return recorduser;
    }

    public void setRecorduser(String recorduser) {
        this.recorduser = recorduser == null ? null : recorduser.trim();
    }

    public Date getRecordtime() {
        return recordtime;
    }

    public void setRecordtime(Date recordtime) {
        this.recordtime = recordtime;
    }

    public String getRevieweruser() {
        return revieweruser;
    }

    public void setRevieweruser(String revieweruser) {
        this.revieweruser = revieweruser == null ? null : revieweruser.trim();
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