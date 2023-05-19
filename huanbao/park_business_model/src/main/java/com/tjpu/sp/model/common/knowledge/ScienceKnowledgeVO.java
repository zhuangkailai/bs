package com.tjpu.sp.model.common.knowledge;

import java.util.Date;

public class ScienceKnowledgeVO {
    private String pkId;

    private String knowledgename;

    private String fkKnowledgetypecode;

    private String knowledgedes;

    private String fkFileid;

    private Date updatetime;

    private String updateuser;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getKnowledgename() {
        return knowledgename;
    }

    public void setKnowledgename(String knowledgename) {
        this.knowledgename = knowledgename == null ? null : knowledgename.trim();
    }

    public String getFkKnowledgetypecode() {
        return fkKnowledgetypecode;
    }

    public void setFkKnowledgetypecode(String fkKnowledgetypecode) {
        this.fkKnowledgetypecode = fkKnowledgetypecode == null ? null : fkKnowledgetypecode.trim();
    }

    public String getKnowledgedes() {
        return knowledgedes;
    }

    public void setKnowledgedes(String knowledgedes) {
        this.knowledgedes = knowledgedes == null ? null : knowledgedes.trim();
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