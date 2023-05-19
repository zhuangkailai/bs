package com.tjpu.sp.model.environmentalprotection.systemhelp;

import java.util.Date;

public class SystemHelpCenterVO {
    private String pkId;

    private String problemname;

    private String resolvent;

    private String fkImgid;

    private String problemtype;

    private Integer orderindex;

    private String updateuser;

    private Date updatetime;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getProblemname() {
        return problemname;
    }

    public void setProblemname(String problemname) {
        this.problemname = problemname == null ? null : problemname.trim();
    }

    public String getResolvent() {
        return resolvent;
    }

    public void setResolvent(String resolvent) {
        this.resolvent = resolvent == null ? null : resolvent.trim();
    }

    public String getFkImgid() {
        return fkImgid;
    }

    public void setFkImgid(String fkImgid) {
        this.fkImgid = fkImgid == null ? null : fkImgid.trim();
    }

    public String getProblemtype() {
        return problemtype;
    }

    public void setProblemtype(String problemtype) {
        this.problemtype = problemtype == null ? null : problemtype.trim();
    }

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
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