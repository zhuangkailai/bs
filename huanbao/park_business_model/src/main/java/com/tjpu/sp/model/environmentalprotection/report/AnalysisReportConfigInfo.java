package com.tjpu.sp.model.environmentalprotection.report;

import java.util.Date;

public class AnalysisReportConfigInfo {
    private String pkId;

    private Short reporttype;

    private String reportattributecode;

    private String reportattributename;

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

    public String getReportattributecode() {
        return reportattributecode;
    }

    public void setReportattributecode(String reportattributecode) {
        this.reportattributecode = reportattributecode == null ? null : reportattributecode.trim();
    }

    public String getReportattributename() {
        return reportattributename;
    }

    public void setReportattributename(String reportattributename) {
        this.reportattributename = reportattributename == null ? null : reportattributename.trim();
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