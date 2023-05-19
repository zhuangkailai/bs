package com.tjpu.sp.model.environmentalprotection.constructionproject;

import java.util.Date;

public class CheckVO {
    private String pkCheckid;

    private String fkPollutionid;

    private String fkChecknaturecode;

    private String fkProjecttypecode;

    private String projectname;

    private String constructionunitname;

    private String fkRegioncode;

    private String fkIndustrytypecode;

    private String approvalnumber;

    private String projectaddress;

    private String acceptnumber;

    private String accepttime;

    private String approvaltime;

    private String fkAuditunitcode;

    private String checknumber;

    private String fkCheckfileid;

    private Double projectinvestment;

    private Double environmentinvestment;

    private String remark;

    private String updatetime;

    private String updateuser;

    public String getPkCheckid() {
        return pkCheckid;
    }

    public void setPkCheckid(String pkCheckid) {
        this.pkCheckid = pkCheckid == null ? null : pkCheckid.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getFkChecknaturecode() {
        return fkChecknaturecode;
    }

    public void setFkChecknaturecode(String fkChecknaturecode) {
        this.fkChecknaturecode = fkChecknaturecode == null ? null : fkChecknaturecode.trim();
    }

    public String getFkProjecttypecode() {
        return fkProjecttypecode;
    }

    public void setFkProjecttypecode(String fkProjecttypecode) {
        this.fkProjecttypecode = fkProjecttypecode == null ? null : fkProjecttypecode.trim();
    }

    public String getProjectname() {
        return projectname;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname == null ? null : projectname.trim();
    }

    public String getConstructionunitname() {
        return constructionunitname;
    }

    public void setConstructionunitname(String constructionunitname) {
        this.constructionunitname = constructionunitname == null ? null : constructionunitname.trim();
    }

    public String getFkRegioncode() {
        return fkRegioncode;
    }

    public void setFkRegioncode(String fkRegioncode) {
        this.fkRegioncode = fkRegioncode == null ? null : fkRegioncode.trim();
    }

    public String getFkIndustrytypecode() {
        return fkIndustrytypecode;
    }

    public void setFkIndustrytypecode(String fkIndustrytypecode) {
        this.fkIndustrytypecode = fkIndustrytypecode == null ? null : fkIndustrytypecode.trim();
    }

    public String getApprovalnumber() {
        return approvalnumber;
    }

    public void setApprovalnumber(String approvalnumber) {
        this.approvalnumber = approvalnumber == null ? null : approvalnumber.trim();
    }

    public String getProjectaddress() {
        return projectaddress;
    }

    public void setProjectaddress(String projectaddress) {
        this.projectaddress = projectaddress == null ? null : projectaddress.trim();
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

    public String getFkAuditunitcode() {
        return fkAuditunitcode;
    }

    public void setFkAuditunitcode(String fkAuditunitcode) {
        this.fkAuditunitcode = fkAuditunitcode == null ? null : fkAuditunitcode.trim();
    }

    public String getChecknumber() {
        return checknumber;
    }

    public void setChecknumber(String checknumber) {
        this.checknumber = checknumber == null ? null : checknumber.trim();
    }

    public String getFkCheckfileid() {
        return fkCheckfileid;
    }

    public void setFkCheckfileid(String fkCheckfileid) {
        this.fkCheckfileid = fkCheckfileid == null ? null : fkCheckfileid.trim();
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
}