package com.tjpu.sp.model.environmentalprotection.licence;

import java.util.Date;

public class UnorganizedGasDischargeLimitVO {
    private String pkId;

    private String fkLicenceid;

    private String fkUnmonitorpointid;

    private String pollutionproductionname;

    private String fkPollutantcode;

    private String pollutantpreventionmeasure;

    private String fkStandardid;

    private Double permithourconcentration;

    private Double permitdayconcentration;

    private String remark;

    private Double permitoneyear;

    private Double permittwoyear;

    private Double permitthreeyear;

    private Double permitfouryear;

    private Double permitfiveyear;

    private Double specialtimelimitflow;

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

    public String getFkUnmonitorpointid() {
        return fkUnmonitorpointid;
    }

    public void setFkUnmonitorpointid(String fkUnmonitorpointid) {
        this.fkUnmonitorpointid = fkUnmonitorpointid == null ? null : fkUnmonitorpointid.trim();
    }

    public String getPollutionproductionname() {
        return pollutionproductionname;
    }

    public void setPollutionproductionname(String pollutionproductionname) {
        this.pollutionproductionname = pollutionproductionname == null ? null : pollutionproductionname.trim();
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public String getPollutantpreventionmeasure() {
        return pollutantpreventionmeasure;
    }

    public void setPollutantpreventionmeasure(String pollutantpreventionmeasure) {
        this.pollutantpreventionmeasure = pollutantpreventionmeasure == null ? null : pollutantpreventionmeasure.trim();
    }

    public String getFkStandardid() {
        return fkStandardid;
    }

    public void setFkStandardid(String fkStandardid) {
        this.fkStandardid = fkStandardid == null ? null : fkStandardid.trim();
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Double getPermitoneyear() {
        return permitoneyear;
    }

    public void setPermitoneyear(Double permitoneyear) {
        this.permitoneyear = permitoneyear;
    }

    public Double getPermittwoyear() {
        return permittwoyear;
    }

    public void setPermittwoyear(Double permittwoyear) {
        this.permittwoyear = permittwoyear;
    }

    public Double getPermitthreeyear() {
        return permitthreeyear;
    }

    public void setPermitthreeyear(Double permitthreeyear) {
        this.permitthreeyear = permitthreeyear;
    }

    public Double getPermitfouryear() {
        return permitfouryear;
    }

    public void setPermitfouryear(Double permitfouryear) {
        this.permitfouryear = permitfouryear;
    }

    public Double getPermitfiveyear() {
        return permitfiveyear;
    }

    public void setPermitfiveyear(Double permitfiveyear) {
        this.permitfiveyear = permitfiveyear;
    }

    public Double getSpecialtimelimitflow() {
        return specialtimelimitflow;
    }

    public void setSpecialtimelimitflow(Double specialtimelimitflow) {
        this.specialtimelimitflow = specialtimelimitflow;
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