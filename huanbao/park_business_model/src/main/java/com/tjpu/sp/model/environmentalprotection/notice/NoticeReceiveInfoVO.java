package com.tjpu.sp.model.environmentalprotection.notice;

public class NoticeReceiveInfoVO {
    private String pkNoticereceiveid;

    private String noticereceiverid;

    private String noticereceivername;

    private String fkNoticeid;

    private Integer isread;

    private NoticeVO noticeVO;

    public NoticeVO getNoticeVO() {
        return noticeVO;
    }

    public void setNoticeVO(NoticeVO noticeVO) {
        this.noticeVO = noticeVO;
    }

    public String getPkNoticereceiveid() {
        return pkNoticereceiveid;
    }

    public void setPkNoticereceiveid(String pkNoticereceiveid) {
        this.pkNoticereceiveid = pkNoticereceiveid == null ? null : pkNoticereceiveid.trim();
    }

    public String getNoticereceiverid() {
        return noticereceiverid;
    }

    public void setNoticereceiverid(String noticereceiverid) {
        this.noticereceiverid = noticereceiverid == null ? null : noticereceiverid.trim();
    }

    public String getNoticereceivername() {
        return noticereceivername;
    }

    public void setNoticereceivername(String noticereceivername) {
        this.noticereceivername = noticereceivername == null ? null : noticereceivername.trim();
    }

    public String getFkNoticeid() {
        return fkNoticeid;
    }

    public void setFkNoticeid(String fkNoticeid) {
        this.fkNoticeid = fkNoticeid == null ? null : fkNoticeid.trim();
    }

    public Integer getIsread() {
        return isread;
    }

    public void setIsread(Integer isread) {
        this.isread = isread;
    }
}