package com.tjpu.sp.model.environmentalprotection.assess;

import java.util.Date;

public class EntAssessmentDataVO {
    private String pkDataid;

    private String fkAssessinfoid;

    private String fkAssessruleid;

    private Double reducescorevalue;

    private String problemdes;

    private String fkFileid;

    private String updateuser;

    private Date updatetime;

    public String getPkDataid() {
        return pkDataid;
    }

    public void setPkDataid(String pkDataid) {
        this.pkDataid = pkDataid == null ? null : pkDataid.trim();
    }

    public String getFkAssessinfoid() {
        return fkAssessinfoid;
    }

    public void setFkAssessinfoid(String fkAssessinfoid) {
        this.fkAssessinfoid = fkAssessinfoid == null ? null : fkAssessinfoid.trim();
    }

    public String getFkAssessruleid() {
        return fkAssessruleid;
    }

    public void setFkAssessruleid(String fkAssessruleid) {
        this.fkAssessruleid = fkAssessruleid == null ? null : fkAssessruleid.trim();
    }

    public Double getReducescorevalue() {
        return reducescorevalue;
    }

    public void setReducescorevalue(Double reducescorevalue) {
        this.reducescorevalue = reducescorevalue;
    }

    public String getProblemdes() {
        return problemdes;
    }

    public void setProblemdes(String problemdes) {
        this.problemdes = problemdes == null ? null : problemdes.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}