package com.tjpu.sp.model.environmentalprotection.tracesource;

import java.util.Date;

public class TraceSourceEntInfoVO {
    private String pkId;

    private String fkPolluteeventid;

    private String fkPollutionid;

    private Short resulttype;

    private Integer ranking;

    private Date updatetime;

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

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid;
    }

    public Short getResulttype() {
        return resulttype;
    }

    public void setResulttype(Short resulttype) {
        this.resulttype = resulttype;
    }

    public Integer getRanking() {
        return ranking;
    }

    public void setRanking(Integer ranking) {
        this.ranking = ranking;
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