package com.tjpu.sp.model.environmentalprotection.monitorstandard;

import java.util.Date;

public class StandardVO {
    private String pkStandardid;

    private String standardname;

    private String standardtype;

    private Date publishdate;

    private Date usedate;

    private String applyrange;

    private Date updatetime;

    private String updateuser;

    private String fkfileid;

    private String publishunit;

    private String Abstract;

    private String keyWords;

    public String getPkStandardid() {
        return pkStandardid;
    }

    public void setPkStandardid(String pkStandardid) {
        this.pkStandardid = pkStandardid == null ? null : pkStandardid.trim();
    }

    public String getStandardname() {
        return standardname;
    }

    public void setStandardname(String standardname) {
        this.standardname = standardname == null ? null : standardname.trim();
    }

    public String getStandardtype() {
        return standardtype;
    }

    public void setStandardtype(String standardtype) {
        this.standardtype = standardtype == null ? null : standardtype.trim();
    }

    public Date getPublishdate() {
        return publishdate;
    }

    public void setPublishdate(Date publishdate) {
        this.publishdate = publishdate;
    }

    public Date getUsedate() {
        return usedate;
    }

    public void setUsedate(Date usedate) {
        this.usedate = usedate;
    }

    public String getApplyrange() {
        return applyrange;
    }

    public void setApplyrange(String applyrange) {
        this.applyrange = applyrange == null ? null : applyrange.trim();
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

    public String getFkfileid() {
        return fkfileid;
    }

    public void setFkfileid(String fkfileid) {
        this.fkfileid = fkfileid;
    }

    public String getPublishunit() {
        return publishunit;
    }

    public void setPublishunit(String publishunit) {
        this.publishunit = publishunit;
    }

    public String getAbstract() {
        return Abstract;
    }

    public void setAbstract(String anAbstract) {
        Abstract = anAbstract;
    }

    public String getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(String keyWords) {
        this.keyWords = keyWords;
    }
}