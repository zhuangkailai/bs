package com.tjpu.sp.model.environmentalprotection.licence;

import java.util.Date;

public class RPExecuteSummaryVO {
    private String pkId;

    private String fkReportid;

    private Integer itemtype;

    private Integer contenttype;

    private Integer contentsubtype;

    private String fkItemid;

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

    public Integer getItemtype() {
        return itemtype;
    }

    public void setItemtype(Integer itemtype) {
        this.itemtype = itemtype;
    }

    public Integer getContenttype() {
        return contenttype;
    }

    public void setContenttype(Integer contenttype) {
        this.contenttype = contenttype;
    }

    public Integer getContentsubtype() {
        return contentsubtype;
    }

    public void setContentsubtype(Integer contentsubtype) {
        this.contentsubtype = contentsubtype;
    }

    public String getFkItemid() {
        return fkItemid;
    }

    public void setFkItemid(String fkItemid) {
        this.fkItemid = fkItemid == null ? null : fkItemid.trim();
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