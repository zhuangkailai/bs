package com.tjpu.sp.model.envhousekeepers.treatmentrunrecord;

import java.util.Date;

public class GasTreatmentRunRecordVO {
    private String pkId;

    private String fkPollutionid;

    private String treatmentname;

    private String treatmentnum;

    private String treatmentmodel;

    private String parametername;

    private Double designvalue;

    private String parameterunit;

    private Date runstarttime;

    private Date runendtime;

    private Short isnormal;

    private Double smokevolume;

    private String fkPollutantcode;

    private Double handleefficiency;

    private String datasources;

    private Double emissionpipeheight;

    private Double outputtemperature;

    private Double pressure;

    private Double flowtime;

    private Double powerconsumption;

    private String afterproduct;

    private String production;

    private String drugname;

    private Date drugaddtime;

    private Double dosage;

    private String recorduser;

    private Date recordtime;

    private String revieweruser;

    private Date updatetime;

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

    public String getTreatmentname() {
        return treatmentname;
    }

    public void setTreatmentname(String treatmentname) {
        this.treatmentname = treatmentname == null ? null : treatmentname.trim();
    }

    public String getTreatmentnum() {
        return treatmentnum;
    }

    public void setTreatmentnum(String treatmentnum) {
        this.treatmentnum = treatmentnum == null ? null : treatmentnum.trim();
    }

    public String getTreatmentmodel() {
        return treatmentmodel;
    }

    public void setTreatmentmodel(String treatmentmodel) {
        this.treatmentmodel = treatmentmodel == null ? null : treatmentmodel.trim();
    }

    public String getParametername() {
        return parametername;
    }

    public void setParametername(String parametername) {
        this.parametername = parametername == null ? null : parametername.trim();
    }

    public Double getDesignvalue() {
        return designvalue;
    }

    public void setDesignvalue(Double designvalue) {
        this.designvalue = designvalue;
    }

    public String getParameterunit() {
        return parameterunit;
    }

    public void setParameterunit(String parameterunit) {
        this.parameterunit = parameterunit == null ? null : parameterunit.trim();
    }

    public Date getRunstarttime() {
        return runstarttime;
    }

    public void setRunstarttime(Date runstarttime) {
        this.runstarttime = runstarttime;
    }

    public Date getRunendtime() {
        return runendtime;
    }

    public void setRunendtime(Date runendtime) {
        this.runendtime = runendtime;
    }

    public Short getIsnormal() {
        return isnormal;
    }

    public void setIsnormal(Short isnormal) {
        this.isnormal = isnormal;
    }

    public Double getSmokevolume() {
        return smokevolume;
    }

    public void setSmokevolume(Double smokevolume) {
        this.smokevolume = smokevolume;
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public Double getHandleefficiency() {
        return handleefficiency;
    }

    public void setHandleefficiency(Double handleefficiency) {
        this.handleefficiency = handleefficiency;
    }

    public String getDatasources() {
        return datasources;
    }

    public void setDatasources(String datasources) {
        this.datasources = datasources == null ? null : datasources.trim();
    }

    public Double getEmissionpipeheight() {
        return emissionpipeheight;
    }

    public void setEmissionpipeheight(Double emissionpipeheight) {
        this.emissionpipeheight = emissionpipeheight;
    }

    public Double getOutputtemperature() {
        return outputtemperature;
    }

    public void setOutputtemperature(Double outputtemperature) {
        this.outputtemperature = outputtemperature;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public Double getFlowtime() {
        return flowtime;
    }

    public void setFlowtime(Double flowtime) {
        this.flowtime = flowtime;
    }

    public Double getPowerconsumption() {
        return powerconsumption;
    }

    public void setPowerconsumption(Double powerconsumption) {
        this.powerconsumption = powerconsumption;
    }

    public String getAfterproduct() {
        return afterproduct;
    }

    public void setAfterproduct(String afterproduct) {
        this.afterproduct = afterproduct == null ? null : afterproduct.trim();
    }

    public String getProduction() {
        return production;
    }

    public void setProduction(String production) {
        this.production = production == null ? null : production.trim();
    }

    public String getDrugname() {
        return drugname;
    }

    public void setDrugname(String drugname) {
        this.drugname = drugname == null ? null : drugname.trim();
    }

    public Date getDrugaddtime() {
        return drugaddtime;
    }

    public void setDrugaddtime(Date drugaddtime) {
        this.drugaddtime = drugaddtime;
    }

    public Double getDosage() {
        return dosage;
    }

    public void setDosage(Double dosage) {
        this.dosage = dosage;
    }

    public String getRecorduser() {
        return recorduser;
    }

    public void setRecorduser(String recorduser) {
        this.recorduser = recorduser == null ? null : recorduser.trim();
    }

    public Date getRecordtime() {
        return recordtime;
    }

    public void setRecordtime(Date recordtime) {
        this.recordtime = recordtime;
    }

    public String getRevieweruser() {
        return revieweruser;
    }

    public void setRevieweruser(String revieweruser) {
        this.revieweruser = revieweruser == null ? null : revieweruser.trim();
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