package com.tjpu.sp.model.environmentalprotection.monitorpoint;

import com.tjpu.sp.model.environmentalprotection.monitorstandard.StandardVO;
import com.tjpu.sp.model.common.pubcode.PollutantFactorVO;

import java.util.Date;
import java.util.List;

public class WaterOutPutPollutantSetVO {
    private String pkDataid;

    private String fkPollutionid;

    private String fkWateroutputid;

    private String fkPollutantcode;

    private String fkStandardid;

    private Double standardmaxvalue;

    private Double standardminvalue;

    private Short monitorway;

    private Short alarmtype;

    private Integer alarmcontroltimes;

    private Double exceptionmaxvalue;

    private Double exceptionminvalue;

    private Integer zerovaluetimes;

    private Integer continuityvaluetimes;

    private Date updatetime;

    private String updateuser;
    private Double flowchangewarnpercent;
    private Double concentrationchangewarnpercent;
    private Double changebasevalue;
    private Double pollutantratio;

    private Integer iseffectivetransmission;

    public Double getPollutantratio() {
        return pollutantratio;
    }

    public void setPollutantratio(Double pollutantratio) {
        this.pollutantratio = pollutantratio;
    }

    //污染物表 一对一 lip
    private PollutantFactorVO pollutantFactorVO;

    public PollutantFactorVO getPollutantFactorVO() {
        return pollutantFactorVO;
    }

    public void setPollutantFactorVO(PollutantFactorVO pollutantFactorVO) {
        this.pollutantFactorVO = pollutantFactorVO;
    }

    //标准表 一对一 lip
    private StandardVO standardVO;

    public StandardVO getStandardVO() {
        return standardVO;
    }

    public void setStandardVO(StandardVO standardVO) {
        this.standardVO = standardVO;
    }

    //预警设置表 一对多 lip
    private List<EarlyWarningSetVO> earlyWarningSetVOS;

    public Double getChangebasevalue() {
        return changebasevalue;
    }

    public void setChangebasevalue(Double changebasevalue) {
        this.changebasevalue = changebasevalue;
    }

    public List<EarlyWarningSetVO> getEarlyWarningSetVOS() {
        return earlyWarningSetVOS;
    }

    public void setEarlyWarningSetVOS(List<EarlyWarningSetVO> earlyWarningSetVOS) {
        this.earlyWarningSetVOS = earlyWarningSetVOS;
    }

    public Double getFlowchangewarnpercent() {
        return flowchangewarnpercent;
    }

    public void setFlowchangewarnpercent(Double flowchangewarnpercent) {
        this.flowchangewarnpercent = flowchangewarnpercent;
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
        this.pkDataid = pkDataid == null ? null : pkDataid.trim();
    }

    public String getFkPollutionid() {
        return fkPollutionid;
    }

    public void setFkPollutionid(String fkPollutionid) {
        this.fkPollutionid = fkPollutionid == null ? null : fkPollutionid.trim();
    }

    public String getFkWateroutputid() {
        return fkWateroutputid;
    }

    public void setFkWateroutputid(String fkWateroutputid) {
        this.fkWateroutputid = fkWateroutputid == null ? null : fkWateroutputid.trim();
    }

    public String getFkPollutantcode() {
        return fkPollutantcode;
    }

    public void setFkPollutantcode(String fkPollutantcode) {
        this.fkPollutantcode = fkPollutantcode == null ? null : fkPollutantcode.trim();
    }

    public String getFkStandardid() {
        return fkStandardid;
    }

    public void setFkStandardid(String fkStandardid) {
        this.fkStandardid = fkStandardid == null ? null : fkStandardid.trim();
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

    public Short getAlarmtype() {
        return alarmtype;
    }

    public void setAlarmtype(Short alarmtype) {
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
        this.updateuser = updateuser == null ? null : updateuser.trim();
    }

    public Integer getIseffectivetransmission() {
        return iseffectivetransmission;
    }

    public void setIseffectivetransmission(Integer iseffectivetransmission) {
        this.iseffectivetransmission = iseffectivetransmission;
    }
}