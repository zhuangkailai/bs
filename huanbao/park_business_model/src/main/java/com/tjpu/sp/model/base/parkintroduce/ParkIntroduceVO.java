package com.tjpu.sp.model.base.parkintroduce;

import java.util.Date;

public class ParkIntroduceVO {
    private String pkParkintroduceid;

    private String parkintroduce;

    private String fkParkmapfileid;

    private String mainindustry;

    private String auxiliaryindustry;

    private Date updatetime;

    private String updateuser;

    public String getPkParkintroduceid() {
        return pkParkintroduceid;
    }

    public void setPkParkintroduceid(String pkParkintroduceid) {
        this.pkParkintroduceid = pkParkintroduceid == null ? null : pkParkintroduceid.trim();
    }

    public String getParkintroduce() {
        return parkintroduce;
    }

    public void setParkintroduce(String parkintroduce) {
        this.parkintroduce = parkintroduce == null ? null : parkintroduce.trim();
    }

    public String getFkParkmapfileid() {
        return fkParkmapfileid;
    }

    public void setFkParkmapfileid(String fkParkmapfileid) {
        this.fkParkmapfileid = fkParkmapfileid == null ? null : fkParkmapfileid.trim();
    }

    public String getMainindustry() {
        return mainindustry;
    }

    public void setMainindustry(String mainindustry) {
        this.mainindustry = mainindustry == null ? null : mainindustry.trim();
    }

    public String getAuxiliaryindustry() {
        return auxiliaryindustry;
    }

    public void setAuxiliaryindustry(String auxiliaryindustry) {
        this.auxiliaryindustry = auxiliaryindustry == null ? null : auxiliaryindustry.trim();
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