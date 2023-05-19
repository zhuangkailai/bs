package com.tjpu.sp.model.environmentalprotection.controlrecords;

import java.util.Date;

public class ControlRecordsVO {
    private String pkId;

    private String dgimn;

    private Date happentime;

    private String controldesc;


    private String pollutantcodes;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn == null ? null : dgimn.trim();
    }

    public Date getHappentime() {
        return happentime;
    }

    public void setHappentime(Date happentime) {
        this.happentime = happentime;
    }

    public String getControldesc() {
        return controldesc;
    }

    public void setControldesc(String controldesc) {
        this.controldesc = controldesc == null ? null : controldesc.trim();
    }

    public String getPollutantcodes() {
        return pollutantcodes;
    }

    public void setPollutantcodes(String pollutantcodes) {
        this.pollutantcodes = pollutantcodes;
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