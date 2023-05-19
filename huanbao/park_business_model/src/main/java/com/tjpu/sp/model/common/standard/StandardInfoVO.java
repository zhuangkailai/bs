package com.tjpu.sp.model.common.standard;

import java.util.Date;

public class StandardInfoVO {
    private String pkStandardid;

    private String standardname;

    private String fkStandardbigtypecode;

    private String fkStandardtypecode;

    private Date publishdate;

    private String publishunit;

    private Date usedate;

    private String abstractcontent;

    private String keywords;

    private String applicabletype;

    private String applyrange;

    private String fkFileid;

    private Date updatetime;

    private String updateuser;

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

    public String getFkStandardbigtypecode() {
        return fkStandardbigtypecode;
    }

    public void setFkStandardbigtypecode(String fkStandardbigtypecode) {
        this.fkStandardbigtypecode = fkStandardbigtypecode == null ? null : fkStandardbigtypecode.trim();
    }

    public String getFkStandardtypecode() {
        return fkStandardtypecode;
    }

    public void setFkStandardtypecode(String fkStandardtypecode) {
        this.fkStandardtypecode = fkStandardtypecode == null ? null : fkStandardtypecode.trim();
    }

    public Date getPublishdate() {
        return publishdate;
    }

    public void setPublishdate(Date publishdate) {
        this.publishdate = publishdate;
    }

    public String getPublishunit() {
        return publishunit;
    }

    public void setPublishunit(String publishunit) {
        this.publishunit = publishunit == null ? null : publishunit.trim();
    }

    public Date getUsedate() {
        return usedate;
    }

    public void setUsedate(Date usedate) {
        this.usedate = usedate;
    }

    public String getAbstractcontent() {
        return abstractcontent;
    }

    public void setAbstractcontent(String abstractcontent) {
        this.abstractcontent = abstractcontent == null ? null : abstractcontent.trim();
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords == null ? null : keywords.trim();
    }

    public String getApplicabletype() {
        return applicabletype;
    }

    public void setApplicabletype(String applicabletype) {
        this.applicabletype = applicabletype == null ? null : applicabletype.trim();
    }

    public String getApplyrange() {
        return applyrange;
    }

    public void setApplyrange(String applyrange) {
        this.applyrange = applyrange == null ? null : applyrange.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
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