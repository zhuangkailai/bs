package com.tjpu.sp.model.environmentalprotection.monitorpoint;

import java.util.List;

public class PollutantSetDataVO {
    private String pkId;
    private String monitorpointid;
    private String pollutantcode;
    private String pollutantname;
    private String standardname;
    private Integer alarmtype;
    private Integer orderindex;
    private Double standardmaxvalue;
    private Double standardminvalue;
    private List<AlarmLevelDataVO> alarmLevelDataVOList;

    public String getPkId() {
        return pkId;
    }

    public void setPkId(String pkId) {
        this.pkId = pkId;
    }

    public String getMonitorpointid() {
        return monitorpointid;
    }

    public void setMonitorpointid(String monitorpointid) {
        this.monitorpointid = monitorpointid;
    }

    public Integer getOrderindex() {
        return orderindex;
    }

    public void setOrderindex(Integer orderindex) {
        this.orderindex = orderindex;
    }

    public String getStandardname() {
        return standardname;
    }

    public void setStandardname(String standardname) {
        this.standardname = standardname;
    }

    public String getPollutantname() {
        return pollutantname;
    }

    public void setPollutantname(String pollutantname) {
        this.pollutantname = pollutantname;
    }

    public String getPollutantcode() {
        return pollutantcode;
    }

    public void setPollutantcode(String pollutantcode) {
        this.pollutantcode = pollutantcode;
    }

    public Integer getAlarmtype() {
        return alarmtype;
    }

    public void setAlarmtype(Integer alarmtype) {
        this.alarmtype = alarmtype;
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

    public List<AlarmLevelDataVO> getAlarmLevelDataVOList() {
        return alarmLevelDataVOList;
    }

    public void setAlarmLevelDataVOList(List<AlarmLevelDataVO> alarmLevelDataVOList) {
        this.alarmLevelDataVOList = alarmLevelDataVOList;
    }
}
