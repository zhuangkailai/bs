package com.tjpu.sp.model.environmentalprotection.radiationsafety;

import java.util.Date;

public class SourcesVO {
    private String pkRadid;

    private String fkLicenceid;

    private String fkWorkplaceid;

    private String fkRadionuclidecode;

    private String fkRadiontypecode;

    private String totalactivity;

    private String fkActivitytypecode;

    private String updateuser;

    private Date updatetime;

    public String getPkRadid() {
        return pkRadid;
    }

    public void setPkRadid(String pkRadid) {
        this.pkRadid = pkRadid == null ? null : pkRadid.trim();
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

    public String getFkRadionuclidecode() {
        return fkRadionuclidecode;
    }

    public void setFkRadionuclidecode(String fkRadionuclidecode) {
        this.fkRadionuclidecode = fkRadionuclidecode == null ? null : fkRadionuclidecode.trim();
    }

    public String getFkRadiontypecode() {
        return fkRadiontypecode;
    }

    public void setFkRadiontypecode(String fkRadiontypecode) {
        this.fkRadiontypecode = fkRadiontypecode == null ? null : fkRadiontypecode.trim();
    }

    public String getTotalactivity() {
        return totalactivity;
    }

    public void setTotalactivity(String totalactivity) {
        this.totalactivity = totalactivity == null ? null : totalactivity.trim();
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