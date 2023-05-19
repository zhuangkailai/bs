package com.tjpu.sp.model.environmentalprotection.notice;

import java.util.Date;
import java.util.List;

public class NoticeVO {
    private String pkNoticeid;

    private String noticetitle;

    private String noticecontent;

    private String fkSenduserid;

    private String sendusername;

    private Date sendtime;

    private String fileid;
    private Integer isrecall;

    public Integer getIsrecall() {
        return isrecall;
    }

    public void setIsrecall(Integer isrecall) {
        this.isrecall = isrecall;
    }

    public String getFileid() {
        return fileid;
    }

    public void setFileid(String fileid) {
        this.fileid = fileid;
    }

    //接收人集合
    private List<NoticeReceiveInfoVO> noticeReceiveInfoVOS;

    public List<NoticeReceiveInfoVO> getNoticeReceiveInfoVOS() {
        return noticeReceiveInfoVOS;
    }

    public void setNoticeReceiveInfoVOS(List<NoticeReceiveInfoVO> noticeReceiveInfoVOS) {
        this.noticeReceiveInfoVOS = noticeReceiveInfoVOS;
    }

    public String getPkNoticeid() {
        return pkNoticeid;
    }

    public void setPkNoticeid(String pkNoticeid) {
        this.pkNoticeid = pkNoticeid == null ? null : pkNoticeid.trim();
    }

    public String getNoticetitle() {
        return noticetitle;
    }

    public void setNoticetitle(String noticetitle) {
        this.noticetitle = noticetitle == null ? null : noticetitle.trim();
    }

    public String getNoticecontent() {
        return noticecontent;
    }

    public void setNoticecontent(String noticecontent) {
        this.noticecontent = noticecontent == null ? null : noticecontent.trim();
    }

    public String getFkSenduserid() {
        return fkSenduserid;
    }

    public void setFkSenduserid(String fkSenduserid) {
        this.fkSenduserid = fkSenduserid == null ? null : fkSenduserid.trim();
    }

    public String getSendusername() {
        return sendusername;
    }

    public void setSendusername(String sendusername) {
        this.sendusername = sendusername == null ? null : sendusername.trim();
    }

    public Date getSendtime() {
        return sendtime;
    }

    public void setSendtime(Date sendtime) {
        this.sendtime = sendtime;
    }
}