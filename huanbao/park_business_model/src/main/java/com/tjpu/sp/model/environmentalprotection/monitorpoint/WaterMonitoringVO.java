package com.tjpu.sp.model.environmentalprotection.monitorpoint;

/**
 * @author: liyc
 * @date:2019/9/19 0019 10:33
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 * @version: V1.0
 */
public class WaterMonitoringVO {
    private String pkwaterId;
    private String fkwatercode;
    private String monitorpointcode;
    private String monitorpointName;
    private String fkcontrollevelcode;
    private String fkwaterbodytypecode;
    private Float longitude;
    private Float latitude;
    private String fkfunwaterqaulitycode;
    private String stationtyear;
    private String fkfileId;
    private Integer status;
    private Integer orderindex;
    private String remark;
    private String dgimn;

    public String getPkwaterId() {
        return pkwaterId;
    }

    public void setPkwaterId(String pkwaterId) {
        this.pkwaterId = pkwaterId;
    }

    public String getFkwatercode() {
        return fkwatercode;
    }

    public void setFkwatercode(String fkwatercode) {
        this.fkwatercode = fkwatercode;
    }

    public String getMonitorpointcode() {
        return monitorpointcode;
    }

    public void setMonitorpointcode(String monitorpointcode) {
        this.monitorpointcode = monitorpointcode;
    }

    public String getMonitorpointName() {
        return monitorpointName;
    }

    public void setMonitorpointName(String monitorpointName) {
        this.monitorpointName = monitorpointName;
    }

    public String getFkcontrollevelcode() {
        return fkcontrollevelcode;
    }

    public void setFkcontrollevelcode(String fkcontrollevelcode) {
        this.fkcontrollevelcode = fkcontrollevelcode;
    }

    public String getFkwaterbodytypecode() {
        return fkwaterbodytypecode;
    }

    public void setFkwaterbodytypecode(String fkwaterbodytypecode) {
        this.fkwaterbodytypecode = fkwaterbodytypecode;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public String getFkfunwaterqaulitycode() {
        return fkfunwaterqaulitycode;
    }

    public void setFkfunwaterqaulitycode(String fkfunwaterqaulitycode) {
        this.fkfunwaterqaulitycode = fkfunwaterqaulitycode;
    }

    public String getStationtyear() {
        return stationtyear;
    }

    public void setStationtyear(String stationtyear) {
        this.stationtyear = stationtyear;
    }

    public String getFkfileId() {
        return fkfileId;
    }

    public void setFkfileId(String fkfileId) {
        this.fkfileId = fkfileId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDgimn() {
        return dgimn;
    }

    public void setDgimn(String dgimn) {
        this.dgimn = dgimn;
    }

    @Override
    public String toString() {
        return "WaterMonitoringVO{" +
                "pkwaterId='" + pkwaterId + '\'' +
                ", fkwatercode='" + fkwatercode + '\'' +
                ", monitorpointcode='" + monitorpointcode + '\'' +
                ", monitorpointName='" + monitorpointName + '\'' +
                ", fkcontrollevelcode='" + fkcontrollevelcode + '\'' +
                ", fkwaterbodytypecode='" + fkwaterbodytypecode + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", fkfunwaterqaulitycode='" + fkfunwaterqaulitycode + '\'' +
                ", stationtyear='" + stationtyear + '\'' +
                ", fkfileId='" + fkfileId + '\'' +
                ", status=" + status +
                ", orderindex=" + orderindex +
                ", remark='" + remark + '\'' +
                ", dgimn='" + dgimn + '\'' +
                '}';
    }
}
