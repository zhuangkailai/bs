package com.tjpu.sp.model.environmentalprotection.radiationsafety;

import java.util.Date;

public class RayDeviceVO {
    private String pkRaydeviceid;

    private String fkLicenceid;

    private String fkWorkplaceid;

    private String raydevicename;

    private String fkRaydevicetype;

    private Integer raydevicequantity;

    private String fkActivitytypecode;

    private String updateuser;

    private Date updatetime;

    public String getPkRaydeviceid() {
        return pkRaydeviceid;
    }

    public void setPkRaydeviceid(String pkRaydeviceid) {
        this.pkRaydeviceid = pkRaydeviceid == null ? null : pkRaydeviceid.trim();
    }

    public String getFkLicenceid() {
        return fkLicenceid;
    }

    public void setFkLicenceid(String fkLicenceid) {
        this.fkLicenceid = fkLicenceid == null ? null : fkLicenceid.trim();
    }

    public String getFkWorkplaceid() {
        return fkWorkplaceid;
    }

    public void setFkWorkplaceid(String fkWorkplaceid) {
        this.fkWorkplaceid = fkWorkplaceid == null ? null : fkWorkplaceid.trim();
    }

    public String getRaydevicename() {
        return raydevicename;
    }

    public void setRaydevicename(String raydevicename) {
        this.raydevicename = raydevicename == null ? null : raydevicename.trim();
    }

    public String getFkRaydevicetype() {
        return fkRaydevicetype;
    }

    public void setFkRaydevicetype(String fkRaydevicetype) {
        this.fkRaydevicetype = fkRaydevicetype == null ? null : fkRaydevicetype.trim();
    }

    public Integer getRaydevicequantity() {
        return raydevicequantity;
    }

    public void setRaydevicequantity(Integer raydevicequantity) {
        this.raydevicequantity = raydevicequantity;
    }

    public String getFkActivitytypecode() {
        return fkActivitytypecode;
    }

    public void setFkActivitytypecode(String fkActivitytypecode) {
        this.fkActivitytypecode = fkActivitytypecode == null ? null : fkActivitytypecode.trim();
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