package com.tjpu.sp.model.environmentalprotection.licence;

import java.util.Date;

public class RPStandingBookRecordDataVO {
    private String pkId;

    private String fkReportid;

    private String fkParamid;

    private Short iscomplete;

    private String remark;

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

    public String getFkParamid() {
        return fkParamid;
    }

    public void setFkParamid(String fkParamid) {
        this.fkParamid = fkParamid == null ? null : fkParamid.trim();
    }

    public Short getIscomplete() {
        return iscomplete;
    }

    public void setIscomplete(Short iscomplete) {
        this.iscomplete = iscomplete;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark == null ? null : remark.trim();
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