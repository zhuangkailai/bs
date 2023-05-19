package com.tjpu.sp.model.envhousekeepers.problemconsult;

import java.util.Date;

public class CommonProblemRecordVO {
    private String pkId;

    private String fkCommonproblemtype;

    private String problemtitle;

    private String replycontent;

    private String recorduser;

    private Date recordtime;

    private Date updatetime;

    private String updateuser;

    private String problemcontent;

    private String fkProblemfileid;

    private String fkReplyfileid;

    private Integer orderindex;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkCommonproblemtype() {
        return fkCommonproblemtype;
    }

    public void setFkCommonproblemtype(String fkCommonproblemtype) {
        this.fkCommonproblemtype = fkCommonproblemtype == null ? null : fkCommonproblemtype.trim();
    }

    public String getProblemtitle() {
        return problemtitle;
    }

    public void setProblemtitle(String problemtitle) {
        this.problemtitle = problemtitle == null ? null : problemtitle.trim();
    }

    public String getReplycontent() {
        return replycontent;
    }

    public void setReplycontent(String replycontent) {
        this.replycontent = replycontent == null ? null : replycontent.trim();
    }

    public String getRecorduser() {
        return recorduser;
    }

    public void setRecorduser(String recorduser) {
        this.recorduser = recorduser == null ? null : recorduser.trim();
    }

    public Date getRecordtime() {
        return recordtime;
    }

    public void setRecordtime(Date recordtime) {
        this.recordtime = recordtime;
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

    public String getProblemcontent() {
        return problemcontent;
    }

    public void setProblemcontent(String problemcontent) {
        this.problemcontent = problemcontent == null ? null : problemcontent.trim();
    }

    public String getFkProblemfileid() {
        return fkProblemfileid;
    }

    public void setFkProblemfileid(String fkProblemfileid) {
        this.fkProblemfileid = fkProblemfileid == null ? null : fkProblemfileid.trim();
    }

    public String getFkReplyfileid() {
        return fkReplyfileid;
    }

    public void setFkReplyfileid(String fkReplyfileid) {
        this.fkReplyfileid = fkReplyfileid == null ? null : fkReplyfileid.trim();
    }

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
    }
}