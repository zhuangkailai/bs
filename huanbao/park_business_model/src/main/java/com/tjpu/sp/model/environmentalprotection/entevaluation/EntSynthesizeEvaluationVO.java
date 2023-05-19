package com.tjpu.sp.model.environmentalprotection.entevaluation;

import java.util.Date;

public class EntSynthesizeEvaluationVO {
    private String pkId;

    private String fkPollutionid;

    private Date evaluationdate;

    private String fkEvaluationlevelcode;

    private Double evaluationscore;

    private String updateuser;

    private Date updatetime;

    private String fkEvaluationschemeid;

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

    public String getFkEvaluationlevelcode() {
        return fkEvaluationlevelcode;
    }

    public void setFkEvaluationlevelcode(String fkEvaluationlevelcode) {
        this.fkEvaluationlevelcode = fkEvaluationlevelcode == null ? null : fkEvaluationlevelcode.trim();
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


    public Double getEvaluationscore() {
        return evaluationscore;
    }

    public void setEvaluationscore(Double evaluationscore) {
        this.evaluationscore = evaluationscore;
    }

    public Date getEvaluationdate() {
        return evaluationdate;
    }

    public void setEvaluationdate(Date evaluationdate) {
        this.evaluationdate = evaluationdate;
    }

    public String getFkEvaluationschemeid() {
        return fkEvaluationschemeid;
    }

    public void setFkEvaluationschemeid(String fkEvaluationschemeid) {
        this.fkEvaluationschemeid = fkEvaluationschemeid;
    }
}