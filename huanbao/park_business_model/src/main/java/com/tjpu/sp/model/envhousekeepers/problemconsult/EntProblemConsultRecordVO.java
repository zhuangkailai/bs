package com.tjpu.sp.model.envhousekeepers.problemconsult;

import java.util.Date;

public class EntProblemConsultRecordVO {
    private String pkId;

    private String fkPollutionid;

    private String problemcontent;

    private String replycontent;

    private String askproblemuser;

    private Date askproblemtime;

    private String replyuser;

    private Date replytime;

    private Date updatetime;

    private String updateuser;

    private String problemtitle;

    private Short status;

    private String fkcommonproblemtype;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getProblemcontent() {
        return problemcontent;
    }

    public void setProblemcontent(String problemcontent) {
        this.problemcontent = problemcontent == null ? null : problemcontent.trim();
    }

    public String getReplycontent() {
        return replycontent;
    }

    public void setReplycontent(String replycontent) {
        this.replycontent = replycontent == null ? null : replycontent.trim();
    }

    public String getAskproblemuser() {
        return askproblemuser;
    }

    public void setAskproblemuser(String askproblemuser) {
        this.askproblemuser = askproblemuser == null ? null : askproblemuser.trim();
    }

    public Date getAskproblemtime() {
        return askproblemtime;
    }

    public void setAskproblemtime(Date askproblemtime) {
        this.askproblemtime = askproblemtime;
    }

    public String getReplyuser() {
        return replyuser;
    }

    public void setReplyuser(String replyuser) {
        this.replyuser = replyuser == null ? null : replyuser.trim();
    }

    public Date getReplytime() {
        return replytime;
    }

    public void setReplytime(Date replytime) {
        this.replytime = replytime;
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

    public String getProblemtitle() {
        return problemtitle;
    }

    public void setProblemtitle(String problemtitle) {
        this.problemtitle = problemtitle == null ? null : problemtitle.trim();
    }

    public Short getStatus() {
        return status;
    }

    public void setStatus(Short status) {
        this.status = status;
    }

    public String getFkcommonproblemtype() {
        return fkcommonproblemtype;
    }

    public void setFkcommonproblemtype(String fkcommonproblemtype) {
        this.fkcommonproblemtype = fkcommonproblemtype;
    }
}