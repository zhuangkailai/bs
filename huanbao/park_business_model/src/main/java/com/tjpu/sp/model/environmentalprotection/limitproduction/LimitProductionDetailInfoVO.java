package com.tjpu.sp.model.environmentalprotection.limitproduction;

import java.util.Date;

public class LimitProductionDetailInfoVO {
    private String pkId;

    private String fkLimitproductionid;

    private String fkOutputid;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public String getFkLimitproductionid() {
        return fkLimitproductionid;
    }

    public void setFkLimitproductionid(String fkLimitproductionid) {
        this.fkLimitproductionid = fkLimitproductionid;
    }

    public String getFkOutputid() {
        return fkOutputid;
    }

    public void setFkOutputid(String fkOutputid) {
        this.fkOutputid = fkOutputid;
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
        this.updateuser = updateuser;
    }
}