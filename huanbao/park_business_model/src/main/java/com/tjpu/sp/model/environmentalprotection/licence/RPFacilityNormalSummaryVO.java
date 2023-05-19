package com.tjpu.sp.model.environmentalprotection.licence;

import java.util.Date;

public class RPFacilityNormalSummaryVO {
    private String pkId;

    private String fkReportid;

    private Integer fkMonitorpointtypecode;

    private String fkFacilityid;

    private Integer facilitytype;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkReportid() {
        return fkReportid;
    }

    public void setFkReportid(String fkReportid) {
        this.fkReportid = fkReportid == null ? null : fkReportid.trim();
    }

    public Integer getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(Integer fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode;
    }

    public String getFkFacilityid() {
        return fkFacilityid;
    }

    public void setFkFacilityid(String fkFacilityid) {
        this.fkFacilityid = fkFacilityid == null ? null : fkFacilityid.trim();
    }

    public Integer getFacilitytype() {
        return facilitytype;
    }

    public void setFacilitytype(Integer facilitytype) {
        this.facilitytype = facilitytype;
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