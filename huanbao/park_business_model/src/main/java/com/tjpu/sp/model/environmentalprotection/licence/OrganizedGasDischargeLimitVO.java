package com.tjpu.sp.model.environmentalprotection.licence;

import java.util.Date;

public class OrganizedGasDischargeLimitVO {
    private String pkId;

    private String fkLicenceid;

    private String outlettype;

    private String fkOutputid;

    private String fkPollutantcode;

    private Double permithourconcentration;

    private Double permitdayconcentration;

    private Double permitrate;

    private Double dischargelimitvalue1;

    private Double dischargelimitvalue2;

    private Double dischargelimitvalue3;

    private Double dischargelimitvalue4;

    private Double dischargelimitvalue5;

    private Double promiseconcentrationlimit;

    private String updateuser;

    private Date updatetime;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkLicenceid() {
        return fkLicenceid;
    }

    public void setFkLicenceid(String fkLicenceid) {
        this.fkLicenceid = fkLicenceid == null ? null : fkLicenceid.trim();
    }

    public String getOutlettype() {
        return outlettype;
    }

    public void setOutlettype(String outlettype) {
        this.outlettype = outlettype == null ? null : outlettype.trim();
    }

    public String getFkOutputid() {
        return fkOutputid;
    }

    public void setFkOutputid(String fkOutputid) {
        this.fkOutputid = fkOutputid == null ? null : fkOutputid.trim();
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public Double getPermithourconcentration() {
        return permithourconcentration;
    }

    public void setPermithourconcentration(Double permithourconcentration) {
        this.permithourconcentration = permithourconcentration;
    }

    public Double getPermitdayconcentration() {
        return permitdayconcentration;
    }

    public void setPermitdayconcentration(Double permitdayconcentration) {
        this.permitdayconcentration = permitdayconcentration;
    }

    public Double getPermitrate() {
        return permitrate;
    }

    public void setPermitrate(Double permitrate) {
        this.permitrate = permitrate;
    }

    public Double getDischargelimitvalue1() {
        return dischargelimitvalue1;
    }

    public void setDischargelimitvalue1(Double dischargelimitvalue1) {
        this.dischargelimitvalue1 = dischargelimitvalue1;
    }

    public Double getDischargelimitvalue2() {
        return dischargelimitvalue2;
    }

    public void setDischargelimitvalue2(Double dischargelimitvalue2) {
        this.dischargelimitvalue2 = dischargelimitvalue2;
    }

    public Double getDischargelimitvalue3() {
        return dischargelimitvalue3;
    }

    public void setDischargelimitvalue3(Double dischargelimitvalue3) {
        this.dischargelimitvalue3 = dischargelimitvalue3;
    }

    public Double getDischargelimitvalue4() {
        return dischargelimitvalue4;
    }

    public void setDischargelimitvalue4(Double dischargelimitvalue4) {
        this.dischargelimitvalue4 = dischargelimitvalue4;
    }

    public Double getDischargelimitvalue5() {
        return dischargelimitvalue5;
    }

    public void setDischargelimitvalue5(Double dischargelimitvalue5) {
        this.dischargelimitvalue5 = dischargelimitvalue5;
    }

    public Double getPromiseconcentrationlimit() {
        return promiseconcentrationlimit;
    }

    public void setPromiseconcentrationlimit(Double promiseconcentrationlimit) {
        this.promiseconcentrationlimit = promiseconcentrationlimit;
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