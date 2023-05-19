package com.tjpu.sp.model.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorstandard.StandardVO;
import com.tjpu.sp.model.common.pubcode.PollutantFactorVO;

import java.util.Date;
import java.util.List;

public class OtherMonitorPointPollutantSetVO {
    private String pkDataid;

    private String fkOthermonintpointid;

    private String fkPollutantcode;

    private String fkStandardid;

    private Double standardmaxvalue;

    private Double standardminvalue;

    private Short monitorway;

    private Integer alarmtype;

    private Integer alarmcontroltimes;

    private Double exceptionmaxvalue;

    private Double exceptionminvalue;

    private Integer zerovaluetimes;

    private Integer continuityvaluetimes;

    private Date updatetime;

    private String updateuser;

    private Double concentrationchangewarnpercent;
    private Double changebasevalue;

    private Double pollutantratio;

    private StandardVO standardVO;

    private PollutantFactorVO pollutantFactorVO;

    private List<EarlyWarningSetVO> earlyWarningSetVOS;

    private Integer iseffectivetransmission;

    public StandardVO getStandardVO() {
        return standardVO;
    }

    public void setStandardVO(StandardVO standardVO) {
        this.standardVO = standardVO;
    }

    public PollutantFactorVO getPollutantFactorVO() {
        return pollutantFactorVO;
    }

    public void setPollutantFactorVO(PollutantFactorVO pollutantFactorVO) {
        this.pollutantFactorVO = pollutantFactorVO;
    }

    public List<EarlyWarningSetVO> getEarlyWarningSetVOS() {
        return earlyWarningSetVOS;
    }

    public void setEarlyWarningSetVOS(List<EarlyWarningSetVO> earlyWarningSetVOS) {
        this.earlyWarningSetVOS = earlyWarningSetVOS;
    }

    public Double getConcentrationchangewarnpercent() {
        return concentrationchangewarnpercent;
    }

    public void setConcentrationchangewarnpercent(Double concentrationchangewarnpercent) {
        this.concentrationchangewarnpercent = concentrationchangewarnpercent;
    }

    public String getPkDataid() {
        return pkDataid;
    }

    public void setPkDataid(String pkDataid) {
        this.pkDataid = pkDataid;
    }

    public String getFkOthermonintpointid() {
        return fkOthermonintpointid;
    }

    public void setFkOthermonintpointid(String fkOthermonintpointid) {
        this.fkOthermonintpointid = fkOthermonintpointid;
    }

    public Double getChangebasevalue() {
        return changebasevalue;
    }

    public void setChangebasevalue(Double changebasevalue) {
        this.changebasevalue = changebasevalue;
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode;
    }

    public String getFkStandardid() {
        return fkStandardid;
    }

    public void setFkStandardid(String fkStandardid) {
        this.fkStandardid = fkStandardid;
    }

    public Double getStandardmaxvalue() {
        return standardmaxvalue;
    }

    public void setStandardmaxvalue(Double standardmaxvalue) {
        this.standardmaxvalue = standardmaxvalue;
    }

    public Double getStandardminvalue() {
        return standardminvalue;
    }

    public void setStandardminvalue(Double standardminvalue) {
        this.standardminvalue = standardminvalue;
    }

    public Short getMonitorway() {
        return monitorway;
    }

    public void setMonitorway(Short monitorway) {
        this.monitorway = monitorway;
    }

    public Integer getAlarmtype() {
        return alarmtype;
    }

    public void setAlarmtype(Integer alarmtype) {
        this.alarmtype = alarmtype;
    }

    public Integer getAlarmcontroltimes() {
        return alarmcontroltimes;
    }

    public void setAlarmcontroltimes(Integer alarmcontroltimes) {
        this.alarmcontroltimes = alarmcontroltimes;
    }

    public Double getExceptionmaxvalue() {
        return exceptionmaxvalue;
    }

    public void setExceptionmaxvalue(Double exceptionmaxvalue) {
        this.exceptionmaxvalue = exceptionmaxvalue;
    }

    public Double getExceptionminvalue() {
        return exceptionminvalue;
    }

    public void setExceptionminvalue(Double exceptionminvalue) {
        this.exceptionminvalue = exceptionminvalue;
    }

    public Integer getZerovaluetimes() {
        return zerovaluetimes;
    }

    public void setZerovaluetimes(Integer zerovaluetimes) {
        this.zerovaluetimes = zerovaluetimes;
    }

    public Integer getContinuityvaluetimes() {
        return continuityvaluetimes;
    }

    public void setContinuityvaluetimes(Integer continuityvaluetimes) {
        this.continuityvaluetimes = continuityvaluetimes;
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
        this.updateuser = updateuser;
    }

    public Double getPollutantratio() {
        return pollutantratio;
    }

    public void setPollutantratio(Double pollutantratio) {
        this.pollutantratio = pollutantratio;
    }

    public Integer getIseffectivetransmission() {
        return iseffectivetransmission;
    }

    public void setIseffectivetransmission(Integer iseffectivetransmission) {
        this.iseffectivetransmission = iseffectivetransmission;
    }
}