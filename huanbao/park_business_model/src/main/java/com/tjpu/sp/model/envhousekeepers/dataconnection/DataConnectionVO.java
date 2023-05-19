package com.tjpu.sp.model.envhousekeepers.dataconnection;

import java.util.Date;

public class DataConnectionVO {
    private String pkId;

    private String fkChecktemplateconfigid;

    private String remark;

    private Short linetype;

    private String url;

    private Integer orderindex;

    private String updateuser;

    private Date updatetime;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkChecktemplateconfigid() {
        return fkChecktemplateconfigid;
    }

    public void setFkChecktemplateconfigid(String fkChecktemplateconfigid) {
        this.fkChecktemplateconfigid = fkChecktemplateconfigid == null ? null : fkChecktemplateconfigid.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Short getLinetype() {
        return linetype;
    }

    public void setLinetype(Short linetype) {
        this.linetype = linetype;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url == null ? null : url.trim();
    }

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
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