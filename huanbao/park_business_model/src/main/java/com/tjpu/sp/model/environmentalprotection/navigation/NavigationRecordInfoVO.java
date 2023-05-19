package com.tjpu.sp.model.environmentalprotection.navigation;

import java.util.Date;

public class NavigationRecordInfoVO {
    private String pkNavigationid;

    private Date navigationdate;

    private Date starttime;

    private Date endtime;

    private Date updatetime;

    private String updateuser;

    private String dgimn;

    public String getPkNavigationid() {
        return pkNavigationid;
    }

    public void setPkNavigationid(String pkNavigationid) {
        this.pkNavigationid = pkNavigationid == null ? null : pkNavigationid.trim();
    }

    public Date getNavigationdate() {
        return navigationdate;
    }

    public void setNavigationdate(Date navigationdate) {
        this.navigationdate = navigationdate;
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }

    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
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

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn == null ? null : dgimn.trim();
    }
}