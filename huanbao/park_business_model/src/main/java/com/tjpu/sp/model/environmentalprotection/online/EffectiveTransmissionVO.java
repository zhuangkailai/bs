package com.tjpu.sp.model.environmentalprotection.online;

import java.util.Date;

public class EffectiveTransmissionVO {
    private String pkId;

    private String fkPollutionid;

    private String fkMonitorpointid;

    private String fkPollutantcode;

    private String fkMonitorpointtypecode;

    private String dgimn;

    private Date countdate;

    private Integer shouldnumber;

    private Integer transmissionnumber;

    private Integer shouldeffectivenumber;

    private Integer effectivenumber;

    private Double transmissionrate;

    private Double effectiverate;

    private Double transmissioneffectiverate;

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

    public String getFkMonitorpointid() {
        return fkMonitorpointid;
    }

    public void setFkMonitorpointid(String fkMonitorpointid) {
        this.fkMonitorpointid = fkMonitorpointid == null ? null : fkMonitorpointid.trim();
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public String getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(String fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode == null ? null : fkMonitorpointtypecode.trim();
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn == null ? null : dgimn.trim();
    }

    public Date getCountdate() {
        return countdate;
    }

    public void setCountdate(Date countdate) {
        this.countdate = countdate;
    }

    public Integer getShouldnumber() {
        return shouldnumber;
    }

    public void setShouldnumber(Integer shouldnumber) {
        this.shouldnumber = shouldnumber;
    }

    public Integer getTransmissionnumber() {
        return transmissionnumber;
    }

    public void setTransmissionnumber(Integer transmissionnumber) {
        this.transmissionnumber = transmissionnumber;
    }

    public Integer getShouldeffectivenumber() {
        return shouldeffectivenumber;
    }

    public void setShouldeffectivenumber(Integer shouldeffectivenumber) {
        this.shouldeffectivenumber = shouldeffectivenumber;
    }

    public Integer getEffectivenumber() {
        return effectivenumber;
    }

    public void setEffectivenumber(Integer effectivenumber) {
        this.effectivenumber = effectivenumber;
    }

    public Double getTransmissionrate() {
        return transmissionrate;
    }

    public void setTransmissionrate(Double transmissionrate) {
        this.transmissionrate = transmissionrate;
    }

    public Double getEffectiverate() {
        return effectiverate;
    }

    public void setEffectiverate(Double effectiverate) {
        this.effectiverate = effectiverate;
    }

    public Double getTransmissioneffectiverate() {
        return transmissioneffectiverate;
    }

    public void setTransmissioneffectiverate(Double transmissioneffectiverate) {
        this.transmissioneffectiverate = transmissioneffectiverate;
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