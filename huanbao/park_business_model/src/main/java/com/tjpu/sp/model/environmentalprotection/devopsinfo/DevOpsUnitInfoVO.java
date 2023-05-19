package com.tjpu.sp.model.environmentalprotection.devopsinfo;

import java.util.Date;

public class DevOpsUnitInfoVO {
    private String pkDevopsunitid;

    private String unitname;

    private String shortername;

    private String corporationname;

    private String entsocialcreditcode;

    private String unitcontactspeople;

    private String unitcontactsphone;

    private String fkDevopsunittype;

    private String fkQualificationtype;

    private String qualificationcode;

    private String unitaddress;

    private Date updatetime;

    private String updateuser;

    public String getPkDevopsunitid() {
        return pkDevopsunitid;
    }

    public void setPkDevopsunitid(String pkDevopsunitid) {
        this.pkDevopsunitid = pkDevopsunitid == null ? null : pkDevopsunitid.trim();
    }

    public String getUnitname() {
        return unitname;
    }

    public void setUnitname(String unitname) {
        this.unitname = unitname == null ? null : unitname.trim();
    }

    public String getShortername() {
        return shortername;
    }

    public void setShortername(String shortername) {
        this.shortername = shortername == null ? null : shortername.trim();
    }

    public String getCorporationname() {
        return corporationname;
    }

    public void setCorporationname(String corporationname) {
        this.corporationname = corporationname == null ? null : corporationname.trim();
    }

    public String getEntsocialcreditcode() {
        return entsocialcreditcode;
    }

    public void setEntsocialcreditcode(String entsocialcreditcode) {
        this.entsocialcreditcode = entsocialcreditcode == null ? null : entsocialcreditcode.trim();
    }

    public String getUnitcontactspeople() {
        return unitcontactspeople;
    }

    public void setUnitcontactspeople(String unitcontactspeople) {
        this.unitcontactspeople = unitcontactspeople == null ? null : unitcontactspeople.trim();
    }

    public String getUnitcontactsphone() {
        return unitcontactsphone;
    }

    public void setUnitcontactsphone(String unitcontactsphone) {
        this.unitcontactsphone = unitcontactsphone == null ? null : unitcontactsphone.trim();
    }

    public String getFkDevopsunittype() {
        return fkDevopsunittype;
    }

    public void setFkDevopsunittype(String fkDevopsunittype) {
        this.fkDevopsunittype = fkDevopsunittype == null ? null : fkDevopsunittype.trim();
    }

    public String getFkQualificationtype() {
        return fkQualificationtype;
    }

    public void setFkQualificationtype(String fkQualificationtype) {
        this.fkQualificationtype = fkQualificationtype == null ? null : fkQualificationtype.trim();
    }

    public String getQualificationcode() {
        return qualificationcode;
    }

    public void setQualificationcode(String qualificationcode) {
        this.qualificationcode = qualificationcode == null ? null : qualificationcode.trim();
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


    public String getUnitaddress() {
        return unitaddress;
    }

    public void setUnitaddress(String unitaddress) {
        this.unitaddress = unitaddress;
    }
}