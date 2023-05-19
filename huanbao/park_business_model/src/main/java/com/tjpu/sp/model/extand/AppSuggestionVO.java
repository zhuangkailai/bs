package com.tjpu.sp.model.extand;

import java.util.Date;

public class AppSuggestionVO {
    private String pkId;

    private Integer apptype;

    private String feedbackuser;

    private String feedbackuseraccount;

    private Date feedbacktime;

    private String feedbacksuggestion;

    private String contactinformation;

    private String fkFileid;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public Integer getApptype() {
        return apptype;
    }

    public void setApptype(Integer apptype) {
        this.apptype = apptype;
    }

    public String getFeedbackuser() {
        return feedbackuser;
    }

    public void setFeedbackuser(String feedbackuser) {
        this.feedbackuser = feedbackuser == null ? null : feedbackuser.trim();
    }

    public String getFeedbackuseraccount() {
        return feedbackuseraccount;
    }

    public void setFeedbackuseraccount(String feedbackuseraccount) {
        this.feedbackuseraccount = feedbackuseraccount == null ? null : feedbackuseraccount.trim();
    }

    public Date getFeedbacktime() {
        return feedbacktime;
    }

    public void setFeedbacktime(Date feedbacktime) {
        this.feedbacktime = feedbacktime;
    }

    public String getFeedbacksuggestion() {
        return feedbacksuggestion;
    }

    public void setFeedbacksuggestion(String feedbacksuggestion) {
        this.feedbacksuggestion = feedbacksuggestion == null ? null : feedbacksuggestion.trim();
    }

    public String getContactinformation() {
        return contactinformation;
    }

    public void setContactinformation(String contactinformation) {
        this.contactinformation = contactinformation == null ? null : contactinformation.trim();
    }

    public String getFkFileid() {
        return fkFileid;
    }

    public void setFkFileid(String fkFileid) {
        this.fkFileid = fkFileid == null ? null : fkFileid.trim();
    }
}