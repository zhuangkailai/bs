package com.tjpu.sp.model.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.common.pubcode.AlarmLevelVO;

import java.util.Date;

public class EarlyWarningSetVO {
    private String pkId;

    private String fkPollutionid;

    private String fkOutputid;

    private String fkPollutantcode;

    private String fkAlarmlevelcode;

    private Double concenalarmminvalue;

    private Double concenalarmmaxvalue;

    private Date updatetime;

    private String updateuser;

    private AlarmLevelVO alarmLevelVO;

    public AlarmLevelVO getAlarmLevelVO() {
        return alarmLevelVO;
    }

    public void setAlarmLevelVO(AlarmLevelVO alarmLevelVO) {
        this.alarmLevelVO = alarmLevelVO;
    }

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

    public String getFkOutputid() {
        return fkOutputid;
    }

    public void setFkOutputid(String fkOutputid) {
        this.fkOutputid = fkOutputid == null ? null : fkOutputid.trim();
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public String getFkAlarmlevelcode() {
        return fkAlarmlevelcode;
    }

    public void setFkAlarmlevelcode(String fkAlarmlevelcode) {
        this.fkAlarmlevelcode = fkAlarmlevelcode == null ? null : fkAlarmlevelcode.trim();
    }

    public Double getConcenalarmminvalue() {
        return concenalarmminvalue;
    }

    public void setConcenalarmminvalue(Double concenalarmminvalue) {
        this.concenalarmminvalue = concenalarmminvalue;
    }

    public Double getConcenalarmmaxvalue() {
        return concenalarmmaxvalue;
    }

    public void setConcenalarmmaxvalue(Double concenalarmmaxvalue) {
        this.concenalarmmaxvalue = concenalarmmaxvalue;
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