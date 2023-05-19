package com.tjpu.sp.model.envhousekeepers;

import java.util.Date;

public class WaterTreatmentFacilityVO {
    private String pkId;

    private String fkPollutionid;

    private String watertype;

    private String pollutantnames;



    private String treatmentnum;

    private String treatmentname;

    private String technologydesc;

    private Double handlewaterquantity;

    private String isfeasiblettechnology;

    private String isinvolvingbsinesssecrets;



    private String othertreatmentinfo;

    private String fkOutputpkid;

    private String outputtype;

    private String remark;

    private Date updatedate;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getWatertype() {
        return watertype;
    }

    public void setWatertype(String watertype) {
        this.watertype = watertype == null ? null : watertype.trim();
    }

    public String getPollutantnames() {
        return pollutantnames;
    }

    public void setPollutantnames(String pollutantnames) {
        this.pollutantnames = pollutantnames == null ? null : pollutantnames.trim();
    }



    public String getTreatmentnum() {
        return treatmentnum;
    }

    public void setTreatmentnum(String treatmentnum) {
        this.treatmentnum = treatmentnum == null ? null : treatmentnum.trim();
    }

    public String getTreatmentname() {
        return treatmentname;
    }

    public void setTreatmentname(String treatmentname) {
        this.treatmentname = treatmentname == null ? null : treatmentname.trim();
    }

    public String getTechnologydesc() {
        return technologydesc;
    }

    public void setTechnologydesc(String technologydesc) {
        this.technologydesc = technologydesc == null ? null : technologydesc.trim();
    }

    public Double getHandlewaterquantity() {
        return handlewaterquantity;
    }

    public void setHandlewaterquantity(Double handlewaterquantity) {
        this.handlewaterquantity = handlewaterquantity;
    }

    public String getIsfeasiblettechnology() {
        return isfeasiblettechnology;
    }

    public void setIsfeasiblettechnology(String isfeasiblettechnology) {
        this.isfeasiblettechnology = isfeasiblettechnology == null ? null : isfeasiblettechnology.trim();
    }

    public String getIsinvolvingbsinesssecrets() {
        return isinvolvingbsinesssecrets;
    }

    public void setIsinvolvingbsinesssecrets(String isinvolvingbsinesssecrets) {
        this.isinvolvingbsinesssecrets = isinvolvingbsinesssecrets == null ? null : isinvolvingbsinesssecrets.trim();
    }



    public String getOthertreatmentinfo() {
        return othertreatmentinfo;
    }

    public void setOthertreatmentinfo(String othertreatmentinfo) {
        this.othertreatmentinfo = othertreatmentinfo == null ? null : othertreatmentinfo.trim();
    }

    public String getFkOutputpkid() {
        return fkOutputpkid;
    }

    public void setFkOutputpkid(String fkOutputpkid) {
        this.fkOutputpkid = fkOutputpkid == null ? null : fkOutputpkid.trim();
    }

    public String getOutputtype() {
        return outputtype;
    }

    public void setOutputtype(String outputtype) {
        this.outputtype = outputtype == null ? null : outputtype.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public Date getUpdatedate() {
        return updatedate;
    }

    public void setUpdatedate(Date updatedate) {
        this.updatedate = updatedate;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }
}