package com.tjpu.sp.model.base.parkbigevent;

import java.util.Date;

public class ParkBigEventVO {
    private String pkBigeventid;

    private Short bigeventyear;

    private String bigeventcontent;

    private Date updatetime;

    private String updateuser;

    public String getPkBigeventid() {
        return pkBigeventid;
    }

    public void setPkBigeventid(String pkBigeventid) {
        this.pkBigeventid = pkBigeventid == null ? null : pkBigeventid.trim();
    }

    public Short getBigeventyear() {
        return bigeventyear;
    }

    public void setBigeventyear(Short bigeventyear) {
        this.bigeventyear = bigeventyear;
    }

    public String getBigeventcontent() {
        return bigeventcontent;
    }

    public void setBigeventcontent(String bigeventcontent) {
        this.bigeventcontent = bigeventcontent == null ? null : bigeventcontent.trim();
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