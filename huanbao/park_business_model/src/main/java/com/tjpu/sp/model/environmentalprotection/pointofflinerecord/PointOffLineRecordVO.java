package com.tjpu.sp.model.environmentalprotection.pointofflinerecord;

import java.util.Date;

public class PointOffLineRecordVO {
    private String pkId;

    private String dgimn;

    private String fkMonitorpointtypecode;

    private Date offlinetime;

    private Date recoverytime;

    private Short isread;

    private String readuser;

    private Date readtime;

    private Date updatetime;

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

    public Date getOfflinetime() {
        return offlinetime;
    }

    public void setOfflinetime(Date offlinetime) {
        this.offlinetime = offlinetime;
    }

    public Date getRecoverytime() {
        return recoverytime;
    }

    public void setRecoverytime(Date recoverytime) {
        this.recoverytime = recoverytime;
    }

    public Short getIsread() {
        return isread;
    }

    public void setIsread(Short isread) {
        this.isread = isread;
    }

    public String getReaduser() {
        return readuser;
    }

    public void setReaduser(String readuser) {
        this.readuser = readuser == null ? null : readuser.trim();
    }

    public Date getReadtime() {
        return readtime;
    }

    public void setReadtime(Date readtime) {
        this.readtime = readtime;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}