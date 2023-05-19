package com.tjpu.sp.model.environmentalprotection.patrol;

import java.util.Date;

public class PatrolTeamUserVO {
    private String pkId;

    private String fkUserid;
    private String fkTeamid;

    public String getFkTeamid() {
        return fkTeamid;
    }

    public void setFkTeamid(String fkTeamid) {
        this.fkTeamid = fkTeamid;
    }

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkUserid() {
        return fkUserid;
    }

    public void setFkUserid(String fkUserid) {
        this.fkUserid = fkUserid == null ? null : fkUserid.trim();
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