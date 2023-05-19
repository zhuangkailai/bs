package com.tjpu.sp.model.environmentalprotection.entevaluation;

import java.util.Date;

public class EntEvaluationDetailVO {
    private String pkId;

    private String fkEntevaluationid;

    private String fkEntevaluationindexid;

    private Double indexevaluationscore;

    private String updateuser;

    private Date updatetime;

    private String remark;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkEntevaluationid() {
        return fkEntevaluationid;
    }

    public void setFkEntevaluationid(String fkEntevaluationid) {
        this.fkEntevaluationid = fkEntevaluationid == null ? null : fkEntevaluationid.trim();
    }

    public Double getIndexevaluationscore() {
        return indexevaluationscore;
    }

    public void setIndexevaluationscore(Double indexevaluationscore) {
        this.indexevaluationscore = indexevaluationscore;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getFkEntevaluationindexid() {
        return fkEntevaluationindexid;
    }

    public void setFkEntevaluationindexid(String fkEntevaluationindexid) {
        this.fkEntevaluationindexid = fkEntevaluationindexid;
    }
}