package com.pub.model;

import java.util.Date;

public class OperateLogVO {
    private String baseOperateId;

    private String baseOperateDb;

    private String baseOperateDt;

    private String baseOperateType;

    private String baseOperatePrimaryKey;

    private String baseOperateContent;

    private String baseOperatePerson;

    private Date baseOperateDatetime;

    private String baseLogType;

    private String baseOperateConfigId;

    public String getBaseOperateId() {
        return baseOperateId;
    }

    public void setBaseOperateId(String baseOperateId) {
        this.baseOperateId = baseOperateId == null ? null : baseOperateId.trim();
    }

    public String getBaseOperateDb() {
        return baseOperateDb;
    }

    public void setBaseOperateDb(String baseOperateDb) {
        this.baseOperateDb = baseOperateDb == null ? null : baseOperateDb.trim();
    }

    public String getBaseOperateDt() {
        return baseOperateDt;
    }

    public void setBaseOperateDt(String baseOperateDt) {
        this.baseOperateDt = baseOperateDt == null ? null : baseOperateDt.trim();
    }

    public String getBaseOperateType() {
        return baseOperateType;
    }

    public void setBaseOperateType(String baseOperateType) {
        this.baseOperateType = baseOperateType == null ? null : baseOperateType.trim();
    }

    public String getBaseOperatePrimaryKey() {
        return baseOperatePrimaryKey;
    }

    public void setBaseOperatePrimaryKey(String baseOperatePrimaryKey) {
        this.baseOperatePrimaryKey = baseOperatePrimaryKey == null ? null : baseOperatePrimaryKey.trim();
    }

    public String getBaseOperateContent() {
        return baseOperateContent;
    }

    public void setBaseOperateContent(String baseOperateContent) {
        this.baseOperateContent = baseOperateContent == null ? null : baseOperateContent.trim();
    }

    public String getBaseOperatePerson() {
        return baseOperatePerson;
    }

    public void setBaseOperatePerson(String baseOperatePerson) {
        this.baseOperatePerson = baseOperatePerson == null ? null : baseOperatePerson.trim();
    }

    public Date getBaseOperateDatetime() {
        return baseOperateDatetime;
    }

    public void setBaseOperateDatetime(Date baseOperateDatetime) {
        this.baseOperateDatetime = baseOperateDatetime;
    }

    public String getBaseLogType() {
        return baseLogType;
    }

    public void setBaseLogType(String baseLogType) {
        this.baseLogType = baseLogType == null ? null : baseLogType.trim();
    }

    public String getBaseOperateConfigId() {
        return baseOperateConfigId;
    }

    public void setBaseOperateConfigId(String baseOperateConfigId) {
        this.baseOperateConfigId = baseOperateConfigId == null ? null : baseOperateConfigId.trim();
    }
}