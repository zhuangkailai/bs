package com.tjpu.sp.model.environmentalprotection.tracesource;

import java.util.Date;

public class TraceSourceConfigInfoVO {
    private String pkId;

    private String attributecode;

    private String attributevalue;

    private String attributename;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getAttributecode() {
        return attributecode;
    }

    public void setAttributecode(String attributecode) {
        this.attributecode = attributecode == null ? null : attributecode.trim();
    }

    public String getAttributevalue() {
        return attributevalue;
    }

    public void setAttributevalue(String attributevalue) {
        this.attributevalue = attributevalue == null ? null : attributevalue.trim();
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

    public String getAttributename() {
        return attributename;
    }

    public void setAttributename(String attributename) {
        this.attributename = attributename;
    }
}