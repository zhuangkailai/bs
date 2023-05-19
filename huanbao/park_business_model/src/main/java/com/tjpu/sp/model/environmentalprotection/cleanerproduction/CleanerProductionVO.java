package com.tjpu.sp.model.environmentalprotection.cleanerproduction;

import java.util.Date;

public class CleanerProductionVO {
    private String pkCleanerproductid;

    private String fkPollutionid;

    private String consultorganizition;

    private String assessdate;

    private String assessorganizition;

    private Integer assessrsult;

    private String checkdate;

    private String checkorganizition;

    private Integer checkrsult;

    private String fkFileid;

    private String updateuser;

    private String updatetime;

    public String getPkCleanerproductid() {
        return pkCleanerproductid;
    }

    public void setPkCleanerproductid(String pkCleanerproductid) {
        this.pkCleanerproductid = pkCleanerproductid == null ? null : pkCleanerproductid.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getConsultorganizition() {
        return consultorganizition;
    }

    public void setConsultorganizition(String consultorganizition) {
        this.consultorganizition = consultorganizition == null ? null : consultorganizition.trim();
    }

    public String getAssessdate() {
        return "".equals(assessdate)?null:assessdate;
    }

    public void setAssessdate(String assessdate) {
        this.assessdate = assessdate;
    }

    public String getAssessorganizition() {
        return assessorganizition;
    }

    public void setAssessorganizition(String assessorganizition) {
        this.assessorganizition = assessorganizition == null ? null : assessorganizition.trim();
    }

    public Integer getAssessrsult() {
        return assessrsult;
    }

    public void setAssessrsult(Integer assessrsult) {
        this.assessrsult = assessrsult;
    }

    public String getCheckdate() {
        return "".equals(checkdate)?null:checkdate;
    }

    public void setCheckdate(String checkdate) {
        this.checkdate = checkdate;
    }

    public String getCheckorganizition() {
        return checkorganizition;
    }

    public void setCheckorganizition(String checkorganizition) {
        this.checkorganizition = checkorganizition == null ? null : checkorganizition.trim();
    }

    public Integer getCheckrsult() {
        return checkrsult;
    }

    public void setCheckrsult(Integer checkrsult) {
        this.checkrsult = checkrsult;
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

    public String getUpdatetime() {
        return "".equals(updatetime)?null:updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }
}