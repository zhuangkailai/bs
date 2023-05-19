package com.tjpu.sp.model.envhousekeepers.entproblemrectifyreport;

import java.util.Date;

public class EntProblemRectifyReportVO {
    private String pkId;

    private String fkPollutionid;

    private Date checktime;

    private String preparationuserid;

    private Date preparationtime;

    private String fkFileid;

    private String updateuser;

    private Date updatedate;

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

    public Date getChecktime() {
        return checktime;
    }

    public void setChecktime(Date checktime) {
        this.checktime = checktime;
    }

    public String getPreparationuserid() {
        return preparationuserid;
    }

    public void setPreparationuserid(String preparationuserid) {
        this.preparationuserid = preparationuserid == null ? null : preparationuserid.trim();
    }

    public Date getPreparationtime() {
        return preparationtime;
    }

    public void setPreparationtime(Date preparationtime) {
        this.preparationtime = preparationtime;
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

    public Date getUpdatedate() {
        return updatedate;
    }

    public void setUpdatedate(Date updatedate) {
        this.updatedate = updatedate;
    }
}