package com.tjpu.sp.model.environmentalprotection.tracesource;

import java.util.Date;

public class TraceSourceResultVO {
    private String pkId;

    private String fkMonitorpointid;

    private String fkMonitorpointtypecode;

    private String suspectedent;

    private String winddirectionname;

    private String describe;

    private String fkFileid;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
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

    public String getSuspectedent() {
        return suspectedent;
    }

    public void setSuspectedent(String suspectedent) {
        this.suspectedent = suspectedent == null ? null : suspectedent.trim();
    }

    public String getWinddirectionname() {
        return winddirectionname;
    }

    public void setWinddirectionname(String winddirectionname) {
        this.winddirectionname = winddirectionname == null ? null : winddirectionname.trim();
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe == null ? null : describe.trim();
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
}