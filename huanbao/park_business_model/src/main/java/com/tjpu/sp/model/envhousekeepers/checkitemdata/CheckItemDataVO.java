package com.tjpu.sp.model.envhousekeepers.checkitemdata;

import java.util.Date;

public class CheckItemDataVO {
    private String pkId;

    private String fkCheckentid;

    private String fkchecktemplateconfigid;

    private String checksituation;

    private String remark;

    private String updateuser;

    private Date updatetime;


    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkCheckentid() {
        return fkCheckentid;
    }

    public void setFkCheckentid(String fkCheckentid) {
        this.fkCheckentid = fkCheckentid == null ? null : fkCheckentid.trim();
    }

    public String getChecksituation() {
        return checksituation;
    }

    public void setChecksituation(String checksituation) {
        this.checksituation = checksituation == null ? null : checksituation.trim();
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

    public String getFkchecktemplateconfigid() {
        return fkchecktemplateconfigid;
    }

    public void setFkchecktemplateconfigid(String fkchecktemplateconfigid) {
        this.fkchecktemplateconfigid = fkchecktemplateconfigid;
    }
}