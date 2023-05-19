package com.tjpu.sp.model.envhousekeepers;

import java.util.Date;

public class EntStandingBookReportVO {
    private String pkId;

    private String fkPollutionid;

    private Integer reporttype;

    private String reportname;

    private String uploaduser;

    private Date uploadtime;

    private String fkFileid;

    private String updateuser;

    private Date updatetime;

    private Date recordstarttime;

    private Date recordendtime;

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

    public Integer getReporttype() {
        return reporttype;
    }

    public void setReporttype(Integer reporttype) {
        this.reporttype = reporttype;
    }

    public String getReportname() {
        return reportname;
    }

    public void setReportname(String reportname) {
        this.reportname = reportname == null ? null : reportname.trim();
    }

    public String getUploaduser() {
        return uploaduser;
    }

    public void setUploaduser(String uploaduser) {
        this.uploaduser = uploaduser == null ? null : uploaduser.trim();
    }

    public Date getUploadtime() {
        return uploadtime;
    }

    public void setUploadtime(Date uploadtime) {
        this.uploadtime = uploadtime;
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
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

    public Date getRecordstarttime() {
        return recordstarttime;
    }

    public void setRecordstarttime(Date recordstarttime) {
        this.recordstarttime = recordstarttime;
    }

    public Date getRecordendtime() {
        return recordendtime;
    }

    public void setRecordendtime(Date recordendtime) {
        this.recordendtime = recordendtime;
    }
}