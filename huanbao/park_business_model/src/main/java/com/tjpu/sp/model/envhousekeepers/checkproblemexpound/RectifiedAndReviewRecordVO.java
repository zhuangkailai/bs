package com.tjpu.sp.model.envhousekeepers.checkproblemexpound;

import java.util.Date;

public class RectifiedAndReviewRecordVO {
    private String pkId;

    private String fkCheckproblemexpoundid;

    private Short managementtype;

    private Date managementtime;

    private String remark;

    private String managementuser;

    private String fkFileid;

    private String updateuser;

    private Date updatetime;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkCheckproblemexpoundid() {
        return fkCheckproblemexpoundid;
    }

    public void setFkCheckproblemexpoundid(String fkCheckproblemexpoundid) {
        this.fkCheckproblemexpoundid = fkCheckproblemexpoundid == null ? null : fkCheckproblemexpoundid.trim();
    }

    public Short getManagementtype() {
        return managementtype;
    }

    public void setManagementtype(Short managementtype) {
        this.managementtype = managementtype;
    }

    public Date getManagementtime() {
        return managementtime;
    }

    public void setManagementtime(Date managementtime) {
        this.managementtime = managementtime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getManagementuser() {
        return managementuser;
    }

    public void setManagementuser(String managementuser) {
        this.managementuser = managementuser == null ? null : managementuser.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
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