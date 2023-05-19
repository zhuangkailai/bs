package com.tjpu.sp.model.environmentalprotection.dischargepermit;

import java.util.Date;

public class LicenceVO {
    private String pkLicenceid;

    private String fkPollutionid;

    private String fkLicenceconditioncode;

    private String fkRegioncode;

    private String licencenum;

    private String licencestartdate;

    private String licenceenddate;

    private String licenceissuedate;

    private String fkIssueunitcode;

    private String fkFileid;

    private String remark;

    private String updatetime;

    private String updateuser;

    public String getPkLicenceid() {
        return pkLicenceid;
    }

    public void setPkLicenceid(String pkLicenceid) {
        this.pkLicenceid = pkLicenceid == null ? null : pkLicenceid.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getFkLicenceconditioncode() {
        return fkLicenceconditioncode;
    }

    public void setFkLicenceconditioncode(String fkLicenceconditioncode) {
        this.fkLicenceconditioncode = fkLicenceconditioncode == null ? null : fkLicenceconditioncode.trim();
    }

    public String getFkRegioncode() {
        return fkRegioncode;
    }

    public void setFkRegioncode(String fkRegioncode) {
        this.fkRegioncode = fkRegioncode == null ? null : fkRegioncode.trim();
    }

    public String getLicencenum() {
        return licencenum;
    }

    public void setLicencenum(String licencenum) {
        this.licencenum = licencenum == null ? null : licencenum.trim();
    }

    public String getLicencestartdate() {
        return "".equals(licencestartdate)?null:licencestartdate;
    }

    public void setLicencestartdate(String licencestartdate) {
        this.licencestartdate = licencestartdate;
    }

    public String getLicenceenddate() {
        return "".equals(licenceenddate)?null:licenceenddate;
    }

    public void setLicenceenddate(String licenceenddate) {
        this.licenceenddate = licenceenddate;
    }

    public String getLicenceissuedate() {
        return "".equals(licenceissuedate)?null:licenceissuedate;
    }

    public void setLicenceissuedate(String licenceissuedate) {
        this.licenceissuedate = licenceissuedate;
    }

    public String getFkIssueunitcode() {
        return fkIssueunitcode;
    }

    public void setFkIssueunitcode(String fkIssueunitcode) {
        this.fkIssueunitcode = fkIssueunitcode == null ? null : fkIssueunitcode.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getUpdatetime() {
        return "".equals(updatetime)?null:updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }
}