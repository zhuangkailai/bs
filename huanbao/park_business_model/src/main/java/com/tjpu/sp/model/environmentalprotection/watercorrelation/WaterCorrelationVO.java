package com.tjpu.sp.model.environmentalprotection.watercorrelation;

import java.util.Date;

public class WaterCorrelationVO {

    private String pkId;

    private String fkWaterpollutionid;

    private String fkWatermonitorpointid;

    private Date starttime;

    private Date endtime;

    private String fkOutfallpollutionid;

    private String fkOutfallmonitorpointid;

    private Date updatetime;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkWaterpollutionid() {
        return fkWaterpollutionid;
    }

    public void setFkWaterpollutionid(String fkWaterpollutionid) {
        this.fkWaterpollutionid = fkWaterpollutionid == null ? null : fkWaterpollutionid.trim();
    }

    public String getFkWatermonitorpointid() {
        return fkWatermonitorpointid;
    }

    public void setFkWatermonitorpointid(String fkWatermonitorpointid) {
        this.fkWatermonitorpointid = fkWatermonitorpointid == null ? null : fkWatermonitorpointid.trim();
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

    public String getFkOutfallpollutionid() {
        return fkOutfallpollutionid;
    }

    public void setFkOutfallpollutionid(String fkOutfallpollutionid) {
        this.fkOutfallpollutionid = fkOutfallpollutionid == null ? null : fkOutfallpollutionid.trim();
    }

    public String getFkOutfallmonitorpointid() {
        return fkOutfallmonitorpointid;
    }

    public void setFkOutfallmonitorpointid(String fkOutfallmonitorpointid) {
        this.fkOutfallmonitorpointid = fkOutfallmonitorpointid == null ? null : fkOutfallmonitorpointid.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
}