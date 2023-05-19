package com.tjpu.sp.model.envhousekeepers.checkcontentdescription;

import java.util.Date;

public class CheckContentDescriptionVO {
    private String pkId;

    private String fkCheckitemdataid;

    private String remark;

    private String fkFileid;

    private String updateuser;

    private Date updatetime;

    private Integer orderindex;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkCheckitemdataid() {
        return fkCheckitemdataid;
    }

    public void setFkCheckitemdataid(String fkCheckitemdataid) {
        this.fkCheckitemdataid = fkCheckitemdataid == null ? null : fkCheckitemdataid.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
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

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
    }
}