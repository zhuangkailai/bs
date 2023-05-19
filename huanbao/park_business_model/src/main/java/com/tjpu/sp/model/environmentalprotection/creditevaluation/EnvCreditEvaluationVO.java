package com.tjpu.sp.model.environmentalprotection.creditevaluation;

public class EnvCreditEvaluationVO {
    private String pkCreditEvaluationid;

    private String fkPollutionid;

    private String evaluationyear;

    private String evaluationrsult;

    private String reviewrsult;

    private String fkFileid;

    private String updateuser;

    private String updatetime;

    public String getPkCreditEvaluationid() {
        return pkCreditEvaluationid;
    }

    public void setPkCreditEvaluationid(String pkCreditEvaluationid) {
        this.pkCreditEvaluationid = pkCreditEvaluationid == null ? null : pkCreditEvaluationid.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getEvaluationyear() {
        return evaluationyear;
    }

    public void setEvaluationyear(String evaluationyear) {
        this.evaluationyear = evaluationyear;
    }

    public String getEvaluationrsult() {
        return evaluationrsult;
    }

    public void setEvaluationrsult(String evaluationrsult) {
        this.evaluationrsult = evaluationrsult;
    }

    public String getReviewrsult() {
        return reviewrsult;
    }

    public void setReviewrsult(String reviewrsult) {
        this.reviewrsult = reviewrsult;
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime == null ? null : updatetime.trim();
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser;
    }
}