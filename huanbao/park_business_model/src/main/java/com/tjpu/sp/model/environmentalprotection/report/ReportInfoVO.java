package com.tjpu.sp.model.environmentalprotection.report;

import java.util.Date;

public class ReportInfoVO {
    private String pkId;

    private String reportnumber;

    private Date reportstarttime;

    private Date reportendtime;

    private Short reporttype;

    private String fkFileid;

    private String updateuser;

    private Date updatedate;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getReportnumber() {
        return reportnumber;
    }

    public void setReportnumber(String reportnumber) {
        this.reportnumber = reportnumber == null ? null : reportnumber.trim();
    }

    public Date getReportstarttime() {
        return reportstarttime;
    }

    public void setReportstarttime(Date reportstarttime) {
        this.reportstarttime = reportstarttime;
    }

    public Date getReportendtime() {
        return reportendtime;
    }

    public void setReportendtime(Date reportendtime) {
        this.reportendtime = reportendtime;
    }

    public Short getReporttype() {
        return reporttype;
    }

    public void setReporttype(Short reporttype) {
        this.reporttype = reporttype;
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

    public Date getUpdatedate() {
        return updatedate;
    }

    public void setUpdatedate(Date updatedate) {
        this.updatedate = updatedate;
    }
}