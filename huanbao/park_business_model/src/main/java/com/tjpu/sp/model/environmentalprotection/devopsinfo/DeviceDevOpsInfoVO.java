package com.tjpu.sp.model.environmentalprotection.devopsinfo;

import java.util.Date;

public class DeviceDevOpsInfoVO {
    private String pkId;

    private String fkPollutionid;

    private String fkMonitorpointid;

    private String fkMonitorpointtypecode;

    private String pollutantcodes;

    private Date devopsstarttime;

    private Date devopsendtime;

    private String devopspeople;

    private String devopsreason;

    private Date updatetime;

    private String updateuser;

    private String devopstype;

    private Date createtime;

    private String fkfileid;

    private Short devopsstatus;

    private String fkdevopscontentcode;
    private String fkentdevopsid;
    private Integer devopspatroltype;

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

    public String getFkMonitorpointid() {
        return fkMonitorpointid;
    }

    public void setFkMonitorpointid(String fkMonitorpointid) {
        this.fkMonitorpointid = fkMonitorpointid == null ? null : fkMonitorpointid.trim();
    }

    public String getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(String fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode == null ? null : fkMonitorpointtypecode.trim();
    }

    public String getPollutantcodes() {
        return pollutantcodes;
    }

    public void setPollutantcodes(String pollutantcodes) {
        this.pollutantcodes = pollutantcodes == null ? null : pollutantcodes.trim();
    }

    public Date getDevopsstarttime() {
        return devopsstarttime;
    }

    public void setDevopsstarttime(Date devopsstarttime) {
        this.devopsstarttime = devopsstarttime;
    }

    public Date getDevopsendtime() {
        return devopsendtime;
    }

    public void setDevopsendtime(Date devopsendtime) {
        this.devopsendtime = devopsendtime;
    }

    public String getDevopspeople() {
        return devopspeople;
    }

    public void setDevopspeople(String devopspeople) {
        this.devopspeople = devopspeople == null ? null : devopspeople.trim();
    }

    public String getDevopsreason() {
        return devopsreason;
    }

    public void setDevopsreason(String devopsreason) {
        this.devopsreason = devopsreason == null ? null : devopsreason.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public String getDevopstype() {
        return devopstype;
    }

    public void setDevopstype(String devopstype) {
        this.devopstype = devopstype;
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public String getFkfileid() {
        return fkfileid;
    }

    public void setFkfileid(String fkfileid) {
        this.fkfileid = fkfileid;
    }

    public Short getDevopsstatus() {
        return devopsstatus;
    }

    public void setDevopsstatus(Short devopsstatus) {
        this.devopsstatus = devopsstatus;
    }

    public String getFkdevopscontentcode() {
        return fkdevopscontentcode;
    }

    public void setFkdevopscontentcode(String fkdevopscontentcode) {
        this.fkdevopscontentcode = fkdevopscontentcode;
    }

    public String getFkentdevopsid() {
        return fkentdevopsid;
    }

    public void setFkentdevopsid(String fkentdevopsid) {
        this.fkentdevopsid = fkentdevopsid;
    }

    public Integer getDevopspatroltype() {
        return devopspatroltype;
    }

    public void setDevopspatroltype(Integer devopspatroltype) {
        this.devopspatroltype = devopspatroltype;
    }
}