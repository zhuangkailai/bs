package com.tjpu.sp.model.environmentalprotection.cjpz;

import java.util.Date;

public class EntConnectSetVO {
    private String pkId;

    private String fkPollutionid;

    private String ip;

    private Integer port;

    private String commportid;

    private Integer baudrate;

    private Integer databits;

    private Double stopbits;

    private Integer parity;

    private String pickway;

    private String remark;

    private String updateuser;

    private Date updatetime;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip == null ? null : ip.trim();
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getCommportid() {
        return commportid;
    }

    public void setCommportid(String commportid) {
        this.commportid = commportid == null ? null : commportid.trim();
    }

    public Integer getBaudrate() {
        return baudrate;
    }

    public void setBaudrate(Integer baudrate) {
        this.baudrate = baudrate;
    }

    public Integer getDatabits() {
        return databits;
    }

    public void setDatabits(Integer databits) {
        this.databits = databits;
    }


    public Integer getParity() {
        return parity;
    }

    public void setParity(Integer parity) {
        this.parity = parity;
    }

    public String getPickway() {
        return pickway;
    }

    public void setPickway(String pickway) {
        this.pickway = pickway == null ? null : pickway.trim();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
    }

    public String getUpdateuser() {
        return updateuser;
    }

    public void setUpdateuser(String updateuser) {
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }

    public Double getStopbits() {
        return stopbits;
    }

    public void setStopbits(Double stopbits) {
        this.stopbits = stopbits;
    }
}