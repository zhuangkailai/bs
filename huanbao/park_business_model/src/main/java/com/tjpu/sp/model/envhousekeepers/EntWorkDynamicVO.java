package com.tjpu.sp.model.envhousekeepers;

import java.util.Date;

public class EntWorkDynamicVO {
    private String pkId;

    private String fkPollutionid;

    private Date dynamictime;

    private String dynamictitle;

    private String dynamicdes;

    private String updateuser;

    private Date updatetime;

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

    public Date getDynamictime() {
        return dynamictime;
    }

    public void setDynamictime(Date dynamictime) {
        this.dynamictime = dynamictime;
    }

    public String getDynamictitle() {
        return dynamictitle;
    }

    public void setDynamictitle(String dynamictitle) {
        this.dynamictitle = dynamictitle == null ? null : dynamictitle.trim();
    }

    public String getDynamicdes() {
        return dynamicdes;
    }

    public void setDynamicdes(String dynamicdes) {
        this.dynamicdes = dynamicdes == null ? null : dynamicdes.trim();
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}