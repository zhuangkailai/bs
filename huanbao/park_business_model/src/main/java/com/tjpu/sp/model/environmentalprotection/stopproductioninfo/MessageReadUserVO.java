package com.tjpu.sp.model.environmentalprotection.stopproductioninfo;

public class MessageReadUserVO {
    private String pkId;

    private String messagetype;

    private String fkRecordid;

    private String userid;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId == null ? null : pkId.trim();
    }

    public String getMessagetype() {
        return messagetype;
    }

    public void setMessagetype(String messagetype) {
        this.messagetype = messagetype == null ? null : messagetype.trim();
    }

    public String getFkRecordid() {
        return fkRecordid;
    }

    public void setFkRecordid(String fkRecordid) {
        this.fkRecordid = fkRecordid == null ? null : fkRecordid.trim();
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid == null ? null : userid.trim();
    }
}