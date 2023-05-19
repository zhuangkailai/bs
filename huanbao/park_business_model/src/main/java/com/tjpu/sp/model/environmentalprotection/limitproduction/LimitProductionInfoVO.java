package com.tjpu.sp.model.environmentalprotection.limitproduction;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class LimitProductionInfoVO {
    private String pkId;

    private String fkPollutionid;

    private String fkMonitorpointtype;

    private String executestarttime;

    private String executeendtime;

    private Double limitproductionpercent;

    private Double benchmarkflow;

    private Integer staggeringpeakstarttimepoint;

    private Integer staggeringpeakendtimepoint;

    private Short isallstop;

    private String fkFileid;

    private String updatetime;

    private String updateuser;

    private String pollutionname;

    private String limitproductionremark;


    private String shortername;

    private Set<Object> limitDetail;

    public String getLimitproductionremark() {
        return limitproductionremark;
    }

    public void setLimitproductionremark(String limitproductionremark) {
        this.limitproductionremark = limitproductionremark;
    }

    public String getShortername() {
        return shortername;
    }

    public void setShortername(String shortername) {
        this.shortername = shortername;
    }

    public String getPollutionname() {
        return pollutionname;
    }

    public void setPollutionname(String pollutionname) {
        this.pollutionname = pollutionname;
    }

    public String getExecutestarttime() {
        return executestarttime;
    }

    public void setExecutestarttime(String executestarttime) {
        this.executestarttime = executestarttime;
    }

    public String getExecuteendtime() {
        return executeendtime;
    }

    public void setExecuteendtime(String executeendtime) {
        this.executeendtime = executeendtime;
    }

    public String getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(String updatetime) {
        this.updatetime = updatetime;
    }

    public Set<Object> getLimitDetail() {
        return limitDetail;
    }

    public void setLimitDetail(Set<Object> limitDetail) {
        this.limitDetail = limitDetail;
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


    public Double getLimitproductionpercent() {
        return limitproductionpercent;
    }

    public void setLimitproductionpercent(Double limitproductionpercent) {
        this.limitproductionpercent = limitproductionpercent;
    }

    public Double getBenchmarkflow() {
        return benchmarkflow;
    }

    public void setBenchmarkflow(Double benchmarkflow) {
        this.benchmarkflow = benchmarkflow;
    }

    public Integer getStaggeringpeakstarttimepoint() {
        return staggeringpeakstarttimepoint;
    }

    public void setStaggeringpeakstarttimepoint(Integer staggeringpeakstarttimepoint) {
        this.staggeringpeakstarttimepoint = staggeringpeakstarttimepoint;
    }

    public Integer getStaggeringpeakendtimepoint() {
        return staggeringpeakendtimepoint;
    }

    public void setStaggeringpeakendtimepoint(Integer staggeringpeakendtimepoint) {
        this.staggeringpeakendtimepoint = staggeringpeakendtimepoint;
    }

    public Short getIsallstop() {
        return isallstop;
    }

    public void setIsallstop(Short isallstop) {
        this.isallstop = isallstop;
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid;
    }


    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser;
    }
}