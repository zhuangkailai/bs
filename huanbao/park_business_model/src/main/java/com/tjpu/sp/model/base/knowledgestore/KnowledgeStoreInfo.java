package com.tjpu.sp.model.base.knowledgestore;

import java.util.Date;

public class KnowledgeStoreInfo {
    private String pkId;

    private String storename;

    private String fkStoretypecode;

    private Double fileid;

    private String remark;

    private Date updatetime;

    private String updateuser;
    private String publishunit;
    private String publishtime;
    private String implementationtime;

    private String Abstract;

    private String keyWords;
    private String applicableType;


    public String getPublishunit() {
        return publishunit;
    }

    public void setPublishunit(String publishunit) {
        this.publishunit = publishunit;
    }

    public String getPublishtime() {
        return publishtime;
    }

    public void setPublishtime(String publishtime) {
        this.publishtime = publishtime;
    }

    public String getImplementationtime() {
        return implementationtime;
    }

    public void setImplementationtime(String implementationtime) {
        this.implementationtime = implementationtime;
    }

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getStorename() {
        return storename;
    }

    public void setStorename(String storename) {
        this.storename = storename == null ? null : storename.trim();
    }

    public String getFkStoretypecode() {
        return fkStoretypecode;
    }

    public void setFkStoretypecode(String fkStoretypecode) {
        this.fkStoretypecode = fkStoretypecode == null ? null : fkStoretypecode.trim();
    }

    public Double getFileid() {
        return fileid;
    }

    public void setFileid(Double fileid) {
        this.fileid = fileid;
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

    public String getApplicableType() {
        return applicableType;
    }

    public void setApplicableType(String applicableType) {
        this.applicableType = applicableType;
    }
}