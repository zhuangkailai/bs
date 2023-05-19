package com.tjpu.sp.model.environmentalprotection.video;

import java.util.Date;

public class VideoOverlayVO {
    private String pkVediooverlayid;

    private String fkVediocameraid;

    private String fkPollutantcode;

    private Integer orderindex;

    private Date updatetime;

    private String updateuser;

    private String overlayposition;

    public String getPkVediooverlayid() {
        return pkVediooverlayid;
    }

    public void setPkVediooverlayid(String pkVediooverlayid) {
        this.pkVediooverlayid = pkVediooverlayid == null ? null : pkVediooverlayid.trim();
    }

    public String getFkVediocameraid() {
        return fkVediocameraid;
    }

    public void setFkVediocameraid(String fkVediocameraid) {
        this.fkVediocameraid = fkVediocameraid == null ? null : fkVediocameraid.trim();
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
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

    public String getOverlayposition() {
        return overlayposition;
    }

    public void setOverlayposition(String overlayposition) {
        this.overlayposition = overlayposition == null ? null : overlayposition.trim();
    }
}