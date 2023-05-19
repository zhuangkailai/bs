package com.tjpu.sp.model.environmentalprotection.entevaluation;

import java.util.Date;

public class EntEvaluationSchemeVO {
    private String pkSchemeid;

    private String schemename;

    private String schemeremark;

    private String updateuser;

    private Date updatetime;

    public String getPkSchemeid() {
        return pkSchemeid;
    }

    public void setPkSchemeid(String pkSchemeid) {
        this.pkSchemeid = pkSchemeid == null ? null : pkSchemeid.trim();
    }

    public String getSchemename() {
        return schemename;
    }

    public void setSchemename(String schemename) {
        this.schemename = schemename == null ? null : schemename.trim();
    }

    public String getSchemeremark() {
        return schemeremark;
    }

    public void setSchemeremark(String schemeremark) {
        this.schemeremark = schemeremark == null ? null : schemeremark.trim();
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