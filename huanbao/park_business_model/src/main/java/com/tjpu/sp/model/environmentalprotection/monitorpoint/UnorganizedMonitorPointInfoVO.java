package com.tjpu.sp.model.environmentalprotection.monitorpoint;

import java.util.Date;

public class UnorganizedMonitorPointInfoVO {
    private String pkId;

    private String fkPollutionid;

    private String productionname;

    private String monitorpointcode;

    private String monitorpointname;

    private String monitorpointposition;

    private Double longitude;

    private Double latitude;

    private String remark;

    private String fkMonitorpointtypecode;

    private Short status;

    private String dgimn;

    private Date updatetime;

    private String updateuser;

    private String fkImgid;

    public String getFkImgid() {
        return fkImgid;
    }

    public void setFkImgid(String fkImgid) {
        this.fkImgid = fkImgid;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid;
    }

    public String getProductionname() {
        return productionname;
    }

    public void setProductionname(String productionname) {
        this.productionname = productionname;
    }

    public String getMonitorpointcode() {
        return monitorpointcode;
    }

    public void setMonitorpointcode(String monitorpointcode) {
        this.monitorpointcode = monitorpointcode;
    }

    public String getMonitorpointname() {
        return monitorpointname;
    }

    public void setMonitorpointname(String monitorpointname) {
        this.monitorpointname = monitorpointname;
    }

    public String getMonitorpointposition() {
        return monitorpointposition;
    }

    public void setMonitorpointposition(String monitorpointposition) {
        this.monitorpointposition = monitorpointposition;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getFkMonitorpointtypecode() {
        return fkMonitorpointtypecode;
    }

    public void setFkMonitorpointtypecode(String fkMonitorpointtypecode) {
        this.fkMonitorpointtypecode = fkMonitorpointtypecode;
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn;
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
        this.updateuser = updateuser;
    }
}