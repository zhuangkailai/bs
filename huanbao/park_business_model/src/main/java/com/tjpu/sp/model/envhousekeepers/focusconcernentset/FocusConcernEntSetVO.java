package com.tjpu.sp.model.envhousekeepers.focusconcernentset;

import java.util.Date;

public class FocusConcernEntSetVO {
    private String pkId;

    private String fkPollutionid;

    private String concernuser;

    private Date concerntime;

    private String remark;

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

    public String getConcernuser() {
        return concernuser;
    }

    public void setConcernuser(String concernuser) {
        this.concernuser = concernuser == null ? null : concernuser.trim();
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

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public Date getConcerntime() {
        return concerntime;
    }

    public void setConcerntime(Date concerntime) {
        this.concerntime = concerntime;
    }
}