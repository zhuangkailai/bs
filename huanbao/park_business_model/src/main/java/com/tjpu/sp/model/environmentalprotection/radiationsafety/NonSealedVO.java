package com.tjpu.sp.model.environmentalprotection.radiationsafety;

import java.util.Date;

public class NonSealedVO {
    private String pkNonsealedmaterialid;

    private String fkLicenceid;

    private String fkWorkplaceid;

    private String fkWorkplacelevelcode;

    private String fkRadionuclidecode;

    private String daymaxoperation;

    private String yearmaxoperation;

    private String fkActivitytypecode;

    private String updateuser;

    private Date updatetime;

    public String getPkNonsealedmaterialid() {
        return pkNonsealedmaterialid;
    }

    public void setPkNonsealedmaterialid(String pkNonsealedmaterialid) {
        this.pkNonsealedmaterialid = pkNonsealedmaterialid == null ? null : pkNonsealedmaterialid.trim();
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

    public String getFkWorkplacelevelcode() {
        return fkWorkplacelevelcode;
    }

    public void setFkWorkplacelevelcode(String fkWorkplacelevelcode) {
        this.fkWorkplacelevelcode = fkWorkplacelevelcode == null ? null : fkWorkplacelevelcode.trim();
    }

    public String getFkRadionuclidecode() {
        return fkRadionuclidecode;
    }

    public void setFkRadionuclidecode(String fkRadionuclidecode) {
        this.fkRadionuclidecode = fkRadionuclidecode == null ? null : fkRadionuclidecode.trim();
    }

    public String getDaymaxoperation() {
        return daymaxoperation;
    }

    public void setDaymaxoperation(String daymaxoperation) {
        this.daymaxoperation = daymaxoperation == null ? null : daymaxoperation.trim();
    }

    public String getYearmaxoperation() {
        return yearmaxoperation;
    }

    public void setYearmaxoperation(String yearmaxoperation) {
        this.yearmaxoperation = yearmaxoperation == null ? null : yearmaxoperation.trim();
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