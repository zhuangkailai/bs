package com.tjpu.sp.model.environmentalprotection.devopsinfo;

import java.util.Date;

public class DevicePersonnelRecordVO {
    private String pkId;

    private String fkEntdevopsid;

    private String fkPersonnelid;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkEntdevopsid() {
        return fkEntdevopsid;
    }

    public void setFkEntdevopsid(String fkEntdevopsid) {
        this.fkEntdevopsid = fkEntdevopsid == null ? null : fkEntdevopsid.trim();
    }

    public String getFkPersonnelid() {
        return fkPersonnelid;
    }

    public void setFkPersonnelid(String fkPersonnelid) {
        this.fkPersonnelid = fkPersonnelid == null ? null : fkPersonnelid.trim();
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