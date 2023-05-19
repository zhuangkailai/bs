package com.tjpu.sp.model.environmentalprotection.constructionproject;

public class ApprovalVO {
    private String pkApprovalid;

    private String fkPollutionid;

    private String constructionunitname;

    private String eiaunitname;

    private String projectname;

    private String fkRegioncode;

    private String fkApprovalclasscode;

    private String fkProjectnaturecode;

    private String fkIndustrytypecode;

    private String projectaddress;

    private Double longitude;

    private Double latitude;

    private String acceptnumber;

    private String accepttime;

    private String approvaltime;

    private String approvalnumber;

    private String fkAuditunitcode;

    private String replyfileid;

    private String reportfileid;

    private String auditfileid;

    private Double projectinvestment;

    private Double environmentinvestment;

    private String remark;

    private String updatetime;

    private String updateuser;

    private String checktime;

    private String checkacceptnumber;

    private String checkapprovalnumber;

    private String fkcheckfileid;

    public String getPkApprovalid() {
        return pkApprovalid;
    }

    public void setPkApprovalid(String pkApprovalid) {
        this.pkApprovalid = pkApprovalid == null ? null : pkApprovalid.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getConstructionunitname() {
        return constructionunitname;
    }

    public void setConstructionunitname(String constructionunitname) {
        this.constructionunitname = constructionunitname == null ? null : constructionunitname.trim();
    }

    public String getEiaunitname() {
        return eiaunitname;
    }

    public void setEiaunitname(String eiaunitname) {
        this.eiaunitname = eiaunitname == null ? null : eiaunitname.trim();
    }

    public String getProjectname() {
        return projectname;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname == null ? null : projectname.trim();
    }

    public String getFkRegioncode() {
        return fkRegioncode;
    }

    public void setFkRegioncode(String fkRegioncode) {
        this.fkRegioncode = fkRegioncode == null ? null : fkRegioncode.trim();
    }

    public String getFkApprovalclasscode() {
        return fkApprovalclasscode;
    }

    public void setFkApprovalclasscode(String fkApprovalclasscode) {
        this.fkApprovalclasscode = fkApprovalclasscode == null ? null : fkApprovalclasscode.trim();
    }

    public String getFkProjectnaturecode() {
        return fkProjectnaturecode;
    }

    public void setFkProjectnaturecode(String fkProjectnaturecode) {
        this.fkProjectnaturecode = fkProjectnaturecode == null ? null : fkProjectnaturecode.trim();
    }

    public String getFkIndustrytypecode() {
        return fkIndustrytypecode;
    }

    public void setFkIndustrytypecode(String fkIndustrytypecode) {
        this.fkIndustrytypecode = fkIndustrytypecode == null ? null : fkIndustrytypecode.trim();
    }

    public String getProjectaddress() {
        return projectaddress;
    }

    public void setProjectaddress(String projectaddress) {
        this.projectaddress = projectaddress == null ? null : projectaddress.trim();
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getAcceptnumber() {
        return acceptnumber;
    }

    public void setAcceptnumber(String acceptnumber) {
        this.acceptnumber = acceptnumber == null ? null : acceptnumber.trim();
    }

    public String getAccepttime() {
        return "".equals(accepttime)?null:accepttime;
    }

    public void setAccepttime(String accepttime) {
        this.accepttime = accepttime;
    }

    public String getApprovaltime() {
        return "".equals(approvaltime)?null:approvaltime;
    }

    public void setApprovaltime(String approvaltime) {
        this.approvaltime = approvaltime;
    }

    public String getApprovalnumber() {
        return approvalnumber;
    }

    public void setApprovalnumber(String approvalnumber) {
        this.approvalnumber = approvalnumber == null ? null : approvalnumber.trim();
    }

    public String getFkAuditunitcode() {
        return fkAuditunitcode;
    }

    public void setFkAuditunitcode(String fkAuditunitcode) {
        this.fkAuditunitcode = fkAuditunitcode == null ? null : fkAuditunitcode.trim();
    }

    public String getReplyfileid() {
        return replyfileid;
    }

    public void setReplyfileid(String replyfileid) {
        this.replyfileid = replyfileid == null ? null : replyfileid.trim();
    }

    public String getReportfileid() {
        return reportfileid;
    }

    public void setReportfileid(String reportfileid) {
        this.reportfileid = reportfileid == null ? null : reportfileid.trim();
    }

    public String getAuditfileid() {
        return auditfileid;
    }

    public void setAuditfileid(String auditfileid) {
        this.auditfileid = auditfileid == null ? null : auditfileid.trim();
    }

    public Double getProjectinvestment() {
        return projectinvestment;
    }

    public void setProjectinvestment(Double projectinvestment) {
        this.projectinvestment = projectinvestment;
    }

    public Double getEnvironmentinvestment() {
        return environmentinvestment;
    }

    public void setEnvironmentinvestment(Double environmentinvestment) {
        this.environmentinvestment = environmentinvestment;
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
        this.updateuser = updateuser;
    }

    public String getChecktime() {
        return checktime;
    }

    public void setChecktime(String checktime) {
        this.checktime = checktime;
    }

    public String getCheckacceptnumber() {
        return checkacceptnumber;
    }

    public void setCheckacceptnumber(String checkacceptnumber) {
        this.checkacceptnumber = checkacceptnumber;
    }

    public String getCheckapprovalnumber() {
        return checkapprovalnumber;
    }

    public void setCheckapprovalnumber(String checkapprovalnumber) {
        this.checkapprovalnumber = checkapprovalnumber;
    }

    public String getFkcheckfileid() {
        return fkcheckfileid;
    }

    public void setFkcheckfileid(String fkcheckfileid) {
        this.fkcheckfileid = fkcheckfileid;
    }
}