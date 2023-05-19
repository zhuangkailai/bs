package com.tjpu.sp.model.envhousekeepers;

import java.util.Date;

public class EntRuleInfoVO {
    private String pkId;

    private String fkPollutionid;

    private String fkRuletypecode;

    private String rulename;

    private String briefcontent;

    private String fkFileid;

    private String updateuser;

    private Date updatetime;

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

    public String getFkRuletypecode() {
        return fkRuletypecode;
    }

    public void setFkRuletypecode(String fkRuletypecode) {
        this.fkRuletypecode = fkRuletypecode == null ? null : fkRuletypecode.trim();
    }

    public String getRulename() {
        return rulename;
    }

    public void setRulename(String rulename) {
        this.rulename = rulename == null ? null : rulename.trim();
    }

    public String getBriefcontent() {
        return briefcontent;
    }

    public void setBriefcontent(String briefcontent) {
        this.briefcontent = briefcontent == null ? null : briefcontent.trim();
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