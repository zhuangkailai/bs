package com.tjpu.sp.model.environmentalprotection.stopproductioninfo;

import java.util.Date;

public class StopProductionInfoVO {
    private String pkId;

    private String fkPollutionid;

    private String fkOutputid;

    private String fkMonitorpointtype;

    private Date starttime;

    private Date endtime;

    private String stopproductionremark;

    private String fkFileid;

    private Date updatetime;

    private String updateuser;

    private String fkStopproductiontype;

    private Date  recoveryproductiontime;

    private String fkRecoveryproductionfileid;

    private String recoveryproductionreason;

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

    public String getFkOutputid() {
        return fkOutputid;
    }

    public void setFkOutputid(String fkOutputid) {
        this.fkOutputid = fkOutputid == null ? null : fkOutputid.trim();
    }

    public String getFkMonitorpointtype() {
        return fkMonitorpointtype;
    }

    public void setFkMonitorpointtype(String fkMonitorpointtype) {
        this.fkMonitorpointtype = fkMonitorpointtype == null ? null : fkMonitorpointtype.trim();
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }

    public String getStopproductionremark() {
        return stopproductionremark;
    }

    public void setStopproductionremark(String stopproductionremark) {
        this.stopproductionremark = stopproductionremark == null ? null : stopproductionremark.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
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

    public String getFkStopproductiontype() {
        return fkStopproductiontype;
    }

    public void setFkStopproductiontype(String fkStopproductiontype) {
        this.fkStopproductiontype = fkStopproductiontype;
    }

    public Date getRecoveryproductiontime() {
        return recoveryproductiontime;
    }

    public void setRecoveryproductiontime(Date recoveryproductiontime) {
        this.recoveryproductiontime = recoveryproductiontime;
    }

    public String getFkRecoveryproductionfileid() {
        return fkRecoveryproductionfileid;
    }

    public void setFkRecoveryproductionfileid(String fkRecoveryproductionfileid) {
        this.fkRecoveryproductionfileid = fkRecoveryproductionfileid;
    }

    public String getRecoveryproductionreason() {
        return recoveryproductionreason;
    }

    public void setRecoveryproductionreason(String recoveryproductionreason) {
        this.recoveryproductionreason = recoveryproductionreason;
    }
}