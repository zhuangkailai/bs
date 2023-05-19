package com.tjpu.sp.model.environmentalprotection.devopsinfo;

import java.util.Date;

public class DevOpsPersonnelVO {
    private String pkPersonnelid;

    private String personnelname;

    private Integer personnelsex;

    private String personnelphone;

    private String personnelrole;

    private String worklicensecode;

    private String fkUserid;

    private Date updatetime;

    private String updateuser;

    private String fkdevopsunitid;

    public String getPkPersonnelid() {
        return pkPersonnelid;
    }

    public void setPkPersonnelid(String pkPersonnelid) {
        this.pkPersonnelid = pkPersonnelid == null ? null : pkPersonnelid.trim();
    }

    public String getPersonnelname() {
        return personnelname;
    }

    public void setPersonnelname(String personnelname) {
        this.personnelname = personnelname == null ? null : personnelname.trim();
    }

    public Integer getPersonnelsex() {
        return personnelsex;
    }

    public void setPersonnelsex(Integer personnelsex) {
        this.personnelsex = personnelsex;
    }

    public String getPersonnelphone() {
        return personnelphone;
    }

    public void setPersonnelphone(String personnelphone) {
        this.personnelphone = personnelphone == null ? null : personnelphone.trim();
    }

    public String getPersonnelrole() {
        return personnelrole;
    }

    public void setPersonnelrole(String personnelrole) {
        this.personnelrole = personnelrole == null ? null : personnelrole.trim();
    }

    public String getWorklicensecode() {
        return worklicensecode;
    }

    public void setWorklicensecode(String worklicensecode) {
        this.worklicensecode = worklicensecode == null ? null : worklicensecode.trim();
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

    public String getFkdevopsunitid() {
        return fkdevopsunitid;
    }

    public void setFkdevopsunitid(String fkdevopsunitid) {
        this.fkdevopsunitid = fkdevopsunitid;
    }
}