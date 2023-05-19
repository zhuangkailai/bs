package com.tjpu.sp.model.envhousekeepers.checkentinfo;

import java.util.Date;

public class EntCheckFeedbackRecordVO {
    private String pkId;

    private String fkPollutionid;

    private Date checktime;

    private String feedbackcontent;

    private String feedbackuser;

    private Date feedbacktime;

    private String updateuser;

    private Date updatetime;

    private Short isupdate;

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

    public Date getChecktime() {
        return checktime;
    }

    public void setChecktime(Date checktime) {
        this.checktime = checktime;
    }

    public String getFeedbackcontent() {
        return feedbackcontent;
    }

    public void setFeedbackcontent(String feedbackcontent) {
        this.feedbackcontent = feedbackcontent == null ? null : feedbackcontent.trim();
    }

    public String getFeedbackuser() {
        return feedbackuser;
    }

    public void setFeedbackuser(String feedbackuser) {
        this.feedbackuser = feedbackuser == null ? null : feedbackuser.trim();
    }

    public Date getFeedbacktime() {
        return feedbacktime;
    }

    public void setFeedbacktime(Date feedbacktime) {
        this.feedbacktime = feedbacktime;
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

    public Short getIsupdate() {
        return isupdate;
    }

    public void setIsupdate(Short isupdate) {
        this.isupdate = isupdate;
    }
}