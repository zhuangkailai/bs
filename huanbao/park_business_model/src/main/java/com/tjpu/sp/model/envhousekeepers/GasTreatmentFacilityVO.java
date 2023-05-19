package com.tjpu.sp.model.envhousekeepers;

import java.util.Date;

public class GasTreatmentFacilityVO {
    private String pkId;

    private String fkFacilityid;

    private String productpollutionname;

    private String pollutantnames;

    private String discharedform;

    private String treatmentnum;

    private String treatmentname;

    private String technologydesc;

    private Double handleefficiency;

    private String isfeasiblettechnology;

    private String isinvolvingbsinesssecrets;

    private String othertreatmentinfo;

    private String fkOutputpkid;

    private String remark;

    private String updateuser;

    private Date updatedate;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkFacilityid() {
        return fkFacilityid;
    }

    public void setFkFacilityid(String fkFacilityid) {
        this.fkFacilityid = fkFacilityid == null ? null : fkFacilityid.trim();
    }

    public String getProductpollutionname() {
        return productpollutionname;
    }

    public void setProductpollutionname(String productpollutionname) {
        this.productpollutionname = productpollutionname == null ? null : productpollutionname.trim();
    }

    public String getPollutantnames() {
        return pollutantnames;
    }

    public void setPollutantnames(String pollutantnames) {
        this.pollutantnames = pollutantnames == null ? null : pollutantnames.trim();
    }

    public String getDischaredform() {
        return discharedform;
    }

    public void setDischaredform(String discharedform) {
        this.discharedform = discharedform == null ? null : discharedform.trim();
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

    public Double getHandleefficiency() {
        return handleefficiency;
    }

    public void setHandleefficiency(Double handleefficiency) {
        this.handleefficiency = handleefficiency;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public Date getUpdatedate() {
        return updatedate;
    }

    public void setUpdatedate(Date updatedate) {
        this.updatedate = updatedate;
    }
}