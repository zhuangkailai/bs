package com.tjpu.sp.model.environmentalprotection.entpermittendflowlimit;

import java.util.Date;

public class EntPermittedFlowLimitValueVO {
    private String pkId;

    private String fkPollutionid;

    private String fkMonitorpointtype;

    private String fkPollutantcode;

    private Integer flowyear;

    private Double totalflow;

    private String updatetime;

    private String updateuser;

    private String pollutionname;

    private String pollutantname;

    public String getPollutionname() {
        return pollutionname;
    }

    public void setPollutionname(String pollutionname) {
        this.pollutionname = pollutionname;
    }

    public String getPollutantname() {
        return pollutantname;
    }

    public void setPollutantname(String pollutantname) {
        this.pollutantname = pollutantname;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid;
    }

    public String getFkMonitorpointtype() {
        return fkMonitorpointtype;
    }

    public void setFkMonitorpointtype(String fkMonitorpointtype) {
        this.fkMonitorpointtype = fkMonitorpointtype;
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode;
    }

    public Integer getFlowyear() {
        return flowyear;
    }

    public void setFlowyear(Integer flowyear) {
        this.flowyear = flowyear;
    }

    public Double getTotalflow() {
        return totalflow;
    }

    public void setTotalflow(Double totalflow) {
        this.totalflow = totalflow;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser;
    }
}