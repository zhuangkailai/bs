package com.tjpu.sp.model.environmentalprotection.video;

import java.util.Date;

public class VideoRecordPlanVO {
    private String pkVideorecordplanid;

    private String fkVediocameraid;

    private String plantranscribeday;

    private String starttime;

    private String endtime;

    private Date updatetime;

    private String updateuser;

    public String getPkVideorecordplanid() {
        return pkVideorecordplanid;
    }

    public void setPkVideorecordplanid(String pkVideorecordplanid) {
        this.pkVideorecordplanid = pkVideorecordplanid == null ? null : pkVideorecordplanid.trim();
    }

    public String getFkVediocameraid() {
        return fkVediocameraid;
    }

    public void setFkVediocameraid(String fkVediocameraid) {
        this.fkVediocameraid = fkVediocameraid == null ? null : fkVediocameraid.trim();
    }

    public String getPlantranscribeday() {
        return plantranscribeday;
    }

    public void setPlantranscribeday(String plantranscribeday) {
        this.plantranscribeday = plantranscribeday == null ? null : plantranscribeday.trim();
    }

    public String getStarttime() {
        return starttime;
    }

    public void setStarttime(String starttime) {
        this.starttime = starttime == null ? null : starttime.trim();
    }

    public String getEndtime() {
        return endtime;
    }

    public void setEndtime(String endtime) {
        this.endtime = endtime == null ? null : endtime.trim();
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