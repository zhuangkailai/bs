package com.tjpu.sp.model.envhousekeepers;

import java.math.BigDecimal;
import java.util.Date;

public class SelfMonitorInfoVO {
    private String pkId;

    private String fkPollutionid;

    private String fkMonitorpointid;

    private String fkPollutantcode;

    private Integer pollutiontype;

    private String monitorcontent;

    private String monitorfacility;

    private String automaticmonitorisnet;

    private String autoinstrumentname;

    private String autoinstrumentpostion;

    private String facilityisrequirement;

    private String methodandnum;

    private String manualfrequency;

    private String manualmethod;

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

    public Integer getPollutiontype() {
        return pollutiontype;
    }

    public void setPollutiontype(Integer pollutiontype) {
        this.pollutiontype = pollutiontype;
    }

    public String getMonitorcontent() {
        return monitorcontent;
    }

    public void setMonitorcontent(String monitorcontent) {
        this.monitorcontent = monitorcontent == null ? null : monitorcontent.trim();
    }

    public String getMonitorfacility() {
        return monitorfacility;
    }

    public void setMonitorfacility(String monitorfacility) {
        this.monitorfacility = monitorfacility;
    }



    public String getAutoinstrumentname() {
        return autoinstrumentname;
    }

    public void setAutoinstrumentname(String autoinstrumentname) {
        this.autoinstrumentname = autoinstrumentname == null ? null : autoinstrumentname.trim();
    }

    public String getAutoinstrumentpostion() {
        return autoinstrumentpostion;
    }

    public void setAutoinstrumentpostion(String autoinstrumentpostion) {
        this.autoinstrumentpostion = autoinstrumentpostion == null ? null : autoinstrumentpostion.trim();
    }

    public String getAutomaticmonitorisnet() {
        return automaticmonitorisnet;
    }

    public void setAutomaticmonitorisnet(String automaticmonitorisnet) {
        this.automaticmonitorisnet = automaticmonitorisnet;
    }

    public String getFacilityisrequirement() {
        return facilityisrequirement;
    }

    public void setFacilityisrequirement(String facilityisrequirement) {
        this.facilityisrequirement = facilityisrequirement;
    }

    public String getMethodandnum() {
        return methodandnum;
    }

    public void setMethodandnum(String methodandnum) {
        this.methodandnum = methodandnum == null ? null : methodandnum.trim();
    }

    public String getManualfrequency() {
        return manualfrequency;
    }

    public void setManualfrequency(String manualfrequency) {
        this.manualfrequency = manualfrequency == null ? null : manualfrequency.trim();
    }

    public String getManualmethod() {
        return manualmethod;
    }

    public void setManualmethod(String manualmethod) {
        this.manualmethod = manualmethod == null ? null : manualmethod.trim();
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