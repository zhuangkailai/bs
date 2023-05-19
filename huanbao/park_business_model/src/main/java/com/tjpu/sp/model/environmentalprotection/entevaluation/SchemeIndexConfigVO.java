package com.tjpu.sp.model.environmentalprotection.entevaluation;

import java.util.Date;

public class SchemeIndexConfigVO {
    private String pkId;

    private String fkSchemeid;

    private String fkEvaluationindexid;

    private String updateuser;

    private Date updatetime;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkSchemeid() {
        return fkSchemeid;
    }

    public void setFkSchemeid(String fkSchemeid) {
        this.fkSchemeid = fkSchemeid == null ? null : fkSchemeid.trim();
    }

    public String getFkEvaluationindexid() {
        return fkEvaluationindexid;
    }

    public void setFkEvaluationindexid(String fkEvaluationindexid) {
        this.fkEvaluationindexid = fkEvaluationindexid == null ? null : fkEvaluationindexid.trim();
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