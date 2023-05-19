package com.tjpu.sp.model.common.emergency;

import java.util.Date;

public class EmergencyCaseInfoVO {
    private String pkId;

    private String casename;

    private String fkEmergencycasetypecode;

    private Date happentime;

    private String address;

    private String fkEmergencyaccidenttypecode;

    private Double longitude;

    private Double latitude;

    private String keywords;

    private String casedes;

    private String fkFileid;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getCasename() {
        return casename;
    }

    public void setCasename(String casename) {
        this.casename = casename == null ? null : casename.trim();
    }

    public String getFkEmergencycasetypecode() {
        return fkEmergencycasetypecode;
    }

    public void setFkEmergencycasetypecode(String fkEmergencycasetypecode) {
        this.fkEmergencycasetypecode = fkEmergencycasetypecode == null ? null : fkEmergencycasetypecode.trim();
    }

    public Date getHappentime() {
        return happentime;
    }

    public void setHappentime(Date happentime) {
        this.happentime = happentime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public String getFkEmergencyaccidenttypecode() {
        return fkEmergencyaccidenttypecode;
    }

    public void setFkEmergencyaccidenttypecode(String fkEmergencyaccidenttypecode) {
        this.fkEmergencyaccidenttypecode = fkEmergencyaccidenttypecode == null ? null : fkEmergencyaccidenttypecode.trim();
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords == null ? null : keywords.trim();
    }

    public String getCasedes() {
        return casedes;
    }

    public void setCasedes(String casedes) {
        this.casedes = casedes == null ? null : casedes.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
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