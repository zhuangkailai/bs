package com.tjpu.sp.model.environmentalprotection.report;

import java.util.Date;

public class AnalysisReportDataVO {
    private String pkId;

    private Short reporttype;

    private Date analysisreportendtime;

    private Date analysisreportstarttime;

    private Date reportmakedate;

    private String reportattributecode;

    private String reportattributevalue;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public Short getReporttype() {
        return reporttype;
    }

    public void setReporttype(Short reporttype) {
        this.reporttype = reporttype;
    }

    public Date getAnalysisreportendtime() {
        return analysisreportendtime;
    }

    public void setAnalysisreportendtime(Date analysisreportendtime) {
        this.analysisreportendtime = analysisreportendtime;
    }

    public Date getAnalysisreportstarttime() {
        return analysisreportstarttime;
    }

    public void setAnalysisreportstarttime(Date analysisreportstarttime) {
        this.analysisreportstarttime = analysisreportstarttime;
    }

    public Date getReportmakedate() {
        return reportmakedate;
    }

    public void setReportmakedate(Date reportmakedate) {
        this.reportmakedate = reportmakedate;
    }

    public String getReportattributecode() {
        return reportattributecode;
    }

    public void setReportattributecode(String reportattributecode) {
        this.reportattributecode = reportattributecode == null ? null : reportattributecode.trim();
    }

    public String getReportattributevalue() {
        return reportattributevalue;
    }

    public void setReportattributevalue(String reportattributevalue) {
        this.reportattributevalue = reportattributevalue == null ? null : reportattributevalue.trim();
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