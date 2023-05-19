package com.tjpu.sp.model.envhousekeepers.gasdischargetotal;

import java.util.Date;

public class GasDischargeTotalVO {
    private String pkId;

    private String fkPollutionid;

    private String fkPollutantcode;

    private String pollutanttype;

    private Integer year;

    private Short counttype;

    private Double dischargevalue;

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

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public String getPollutanttype() {
        return pollutanttype;
    }

    public void setPollutanttype(String pollutanttype) {
        this.pollutanttype = pollutanttype == null ? null : pollutanttype.trim();
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }


    public Double getDischargevalue() {
        return dischargevalue;
    }

    public void setDischargevalue(Double dischargevalue) {
        this.dischargevalue = dischargevalue;
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

    public Short getCounttype() {
        return counttype;
    }

    public void setCounttype(Short counttype) {
        this.counttype = counttype;
    }
}