package com.tjpu.sp.model.environmentalprotection.punishment;

import java.util.Date;

public class PunishmentVO {
    private String pkCaseid;

    private String fkPollutionid;

    private String registercode;

    private String filingtime;

    private String casename;

    private String fkPunishunitcode;

    private String fkCasetypecode;

    private String fkIllegaltypecode;

    private String projectname;

    private String illegalact;

    private String rectifysituation;

    private String punishfilenum;

    private String punishmentsituation;

    private String opensituation;

    private Double punishtotalmoney;

    private String executesituation;

    private Integer isend;

    private String fkFileid;

    private String remark;

    private String updateuser;

    private String updatetime;

    public String getPkCaseid() {
        return pkCaseid;
    }

    public void setPkCaseid(String pkCaseid) {
        this.pkCaseid = pkCaseid == null ? null : pkCaseid.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getRegistercode() {
        return registercode;
    }

    public void setRegistercode(String registercode) {
        this.registercode = registercode == null ? null : registercode.trim();
    }

    public String getFilingtime() {
        return "".equals(filingtime)?null:filingtime;
    }

    public void setFilingtime(String filingtime) {
        this.filingtime = filingtime;
    }

    public String getCasename() {
        return casename;
    }

    public void setCasename(String casename) {
        this.casename = casename == null ? null : casename.trim();
    }

    public String getFkPunishunitcode() {
        return fkPunishunitcode;
    }

    public void setFkPunishunitcode(String fkPunishunitcode) {
        this.fkPunishunitcode = fkPunishunitcode == null ? null : fkPunishunitcode.trim();
    }

    public String getFkCasetypecode() {
        return fkCasetypecode;
    }

    public void setFkCasetypecode(String fkCasetypecode) {
        this.fkCasetypecode = fkCasetypecode == null ? null : fkCasetypecode.trim();
    }

    public String getFkIllegaltypecode() {
        return fkIllegaltypecode;
    }

    public void setFkIllegaltypecode(String fkIllegaltypecode) {
        this.fkIllegaltypecode = fkIllegaltypecode == null ? null : fkIllegaltypecode.trim();
    }

    public String getProjectname() {
        return projectname;
    }

    public void setProjectname(String projectname) {
        this.projectname = projectname == null ? null : projectname.trim();
    }

    public String getIllegalact() {
        return illegalact;
    }

    public void setIllegalact(String illegalact) {
        this.illegalact = illegalact == null ? null : illegalact.trim();
    }

    public String getRectifysituation() {
        return rectifysituation;
    }

    public void setRectifysituation(String rectifysituation) {
        this.rectifysituation = rectifysituation == null ? null : rectifysituation.trim();
    }

    public String getPunishfilenum() {
        return punishfilenum;
    }

    public void setPunishfilenum(String punishfilenum) {
        this.punishfilenum = punishfilenum == null ? null : punishfilenum.trim();
    }

    public String getPunishmentsituation() {
        return punishmentsituation;
    }

    public void setPunishmentsituation(String punishmentsituation) {
        this.punishmentsituation = punishmentsituation == null ? null : punishmentsituation.trim();
    }

    public String getOpensituation() {
        return opensituation;
    }

    public void setOpensituation(String opensituation) {
        this.opensituation = opensituation == null ? null : opensituation.trim();
    }

    public Double getPunishtotalmoney() {
        return punishtotalmoney;
    }

    public void setPunishtotalmoney(Double punishtotalmoney) {
        this.punishtotalmoney = punishtotalmoney;
    }

    public String getExecutesituation() {
        return executesituation;
    }

    public void setExecutesituation(String executesituation) {
        this.executesituation = executesituation == null ? null : executesituation.trim();
    }

    public Integer getIsend() {
        return isend;
    }

    public void setIsend(Integer isend) {
        this.isend = isend;
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