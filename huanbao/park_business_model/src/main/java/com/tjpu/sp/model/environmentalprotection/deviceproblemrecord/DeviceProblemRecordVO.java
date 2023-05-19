package com.tjpu.sp.model.environmentalprotection.deviceproblemrecord;

import java.util.Date;

public class DeviceProblemRecordVO {
    private String pkId;

    private String dgimn;

    private String fkMonitorpointtypecode;

    private Short problemtype;

    private String problemremark;

    private Date updatetime;

    private Date transferdate;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn == null ? null : dgimn.trim();
    }

    public String getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(String fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode == null ? null : fkMonitorpointtypecode.trim();
    }

    public Short getProblemtype() {
        return problemtype;
    }

    public void setProblemtype(Short problemtype) {
        this.problemtype = problemtype;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public String getProblemremark() {
        return problemremark;
    }

    public void setProblemremark(String problemremark) {
        this.problemremark = problemremark;
    }

    public Date getTransferdate() {
        return transferdate;
    }

    public void setTransferdate(Date transferdate) {
        this.transferdate = transferdate;
    }
}