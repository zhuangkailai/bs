package com.tjpu.sp.model.environmentalprotection.tracesource;


public class PollutaEventDetailInfoVO {
    private String pkId;

    private String fkPolluteeventid;

    private String fkPollutantcode;

    private String fkMonitorpointid;

    private String updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public String getFkPolluteeventid() {
        return fkPolluteeventid;
    }

    public void setFkPolluteeventid(String fkPolluteeventid) {
        this.fkPolluteeventid = fkPolluteeventid;
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode;
    }

    public String getFkMonitorpointid() {
        return fkMonitorpointid;
    }

    public void setFkMonitorpointid(String fkMonitorpointid) {
        this.fkMonitorpointid = fkMonitorpointid;
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