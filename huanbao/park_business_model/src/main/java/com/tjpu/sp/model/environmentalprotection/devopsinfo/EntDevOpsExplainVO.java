package com.tjpu.sp.model.environmentalprotection.devopsinfo;

import java.util.Date;

public class EntDevOpsExplainVO {
    private String pkId;

    private String fkMonitorpointid;

    private String fkMonitorpointtypecode;

    private Date starttime;

    private Date endtime;

    private Date updatetime;

    private String updateuser;

    private String remark;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }



    public String getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(String fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode == null ? null : fkMonitorpointtypecode.trim();
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getFkMonitorpointid() {
        return fkMonitorpointid;
    }

    public void setFkMonitorpointid(String fkMonitorpointid) {
        this.fkMonitorpointid = fkMonitorpointid;
    }
}