package com.tjpu.sp.model.environmentalprotection.particularpollutants;

import java.util.Date;

public class ParticularPollutantsVO {
    private String pkDataid;

    private String fkPollutionid;

    private String fkOutputid;

    private String fkMonitorpointtypecode;

    private String fkPollutantcode;

    private Date detectiontime;

    private Double detectionconcentration;

    private Short ismainpollutant;

    private String version;


    private Date updatetime;

    private String updateuser;


    public String getPkDataid() {
        return pkDataid;
    }

    public void setPkDataid(String pkDataid) {
        this.pkDataid = pkDataid;
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid;
    }

    public String getFkOutputid() {
        return fkOutputid;
    }

    public void setFkOutputid(String fkOutputid) {
        this.fkOutputid = fkOutputid;
    }

    public String getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(String fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode;
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode;
    }

    public Date getDetectiontime() {
        return detectiontime;
    }

    public void setDetectiontime(Date detectiontime) {
        this.detectiontime = detectiontime;
    }

    public Double getDetectionconcentration() {
        return detectionconcentration;
    }

    public void setDetectionconcentration(Double detectionconcentration) {
        this.detectionconcentration = detectionconcentration;
    }

    public Short getIsmainpollutant() {
        return ismainpollutant;
    }

    public void setIsmainpollutant(Short ismainpollutant) {
        this.ismainpollutant = ismainpollutant;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
        this.updateuser = updateuser;
    }
}