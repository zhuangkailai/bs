package com.tjpu.sp.model.common.knowledge;

import java.util.Date;

public class TrainUserInfoVO {
    private String pkId;

    private String fkTrainid;

    private String fkUserid;

    private Date studytime;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkTrainid() {
        return fkTrainid;
    }

    public void setFkTrainid(String fkTrainid) {
        this.fkTrainid = fkTrainid == null ? null : fkTrainid.trim();
    }

    public String getFkUserid() {
        return fkUserid;
    }

    public void setFkUserid(String fkUserid) {
        this.fkUserid = fkUserid == null ? null : fkUserid.trim();
    }

    public Date getStudytime() {
        return studytime;
    }

    public void setStudytime(Date studytime) {
        this.studytime = studytime;
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