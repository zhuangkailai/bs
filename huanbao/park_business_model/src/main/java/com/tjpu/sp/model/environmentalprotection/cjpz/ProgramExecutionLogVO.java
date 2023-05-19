package com.tjpu.sp.model.environmentalprotection.cjpz;

import java.util.Date;

public class ProgramExecutionLogVO {
    private String pkId;

    private String fkPollutionid;

    private String ip;

    private Integer port;

    private Date errorstime;

    private String errorslog;

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

    public Date getErrorstime() {
        return errorstime;
    }

    public void setErrorstime(Date errorstime) {
        this.errorstime = errorstime;
    }

    public String getErrorslog() {
        return errorslog;
    }

    public void setErrorslog(String errorslog) {
        this.errorslog = errorslog == null ? null : errorslog.trim();
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
}