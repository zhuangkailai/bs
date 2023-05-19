package com.tjpu.sp.model.environmentalprotection.dangerwaste;

public class DangerWasteLicenceInfoVO {
    private String pkLicenceid;

    private String fkPollutionid;

    private String licencenum;

    private String licencestartdate;

    private String licenceenddate;

    private String licenceissuedate;

    private String licencefirstissuedate;

    private String fkWasteoperationmodescode;

    private String operfacilitiesaddress;

    private String fkIssueunitcode;

    private String fkFileid;

    private String remark;

    private String updateuser;

    private String updatetime;

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

    public String getLicencefirstissuedate() {
        return "".equals(licencefirstissuedate)?null:licencefirstissuedate;
    }

    public void setLicencefirstissuedate(String licencefirstissuedate) {
        this.licencefirstissuedate = licencefirstissuedate;
    }

    public String getFkWasteoperationmodescode() {
        return fkWasteoperationmodescode;
    }

    public void setFkWasteoperationmodescode(String fkWasteoperationmodescode) {
        this.fkWasteoperationmodescode = fkWasteoperationmodescode == null ? null : fkWasteoperationmodescode.trim();
    }

    public String getOperfacilitiesaddress() {
        return operfacilitiesaddress;
    }

    public void setOperfacilitiesaddress(String operfacilitiesaddress) {
        this.operfacilitiesaddress = operfacilitiesaddress == null ? null : operfacilitiesaddress.trim();
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