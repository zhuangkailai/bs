package com.tjpu.sp.model.common.mongodb;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author: chengzq
 * @date: 2019/5/17 0017 13:26
 * @Description:
 * @updateUser:
 * @updateDate:
 * @updateDescription:
 */
@Component
@Document(collection = "OverData")
public class AlarmDataVO {

    @Id
    private String id;
    @Field("PollutantCode")
    private String pollutantCode;
    @Field("OverTime")
    private Date overTime;
    @Field("DataType")
    private String dataType;
    @Field("AlarmType")
    private String alarmType;
    @Field("AlarmLevel")
    private Integer alarmLevel;
    @Field("MonitorValue")
    private String monitorValue;
    @Field("DataGatherCode")
    private String dataGatherCode;
    @Field("IsOverStandard")
    private Boolean isOverStandard;


    public Boolean getOverStandard() {
        return isOverStandard;
    }

    public void setOverStandard(Boolean overStandard) {
        isOverStandard = overStandard;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPollutantCode() {
        return pollutantCode;
    }

    public void setPollutantCode(String pollutantCode) {
        this.pollutantCode = pollutantCode;
    }

    public Date getOverTime() {
        return overTime;
    }

    public void setOverTime(Date overTime) {
        this.overTime = overTime;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public Integer getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(Integer alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    public String getMonitorValue() {
        return monitorValue;
    }

    public void setMonitorValue(String monitorValue) {
        this.monitorValue = monitorValue;
    }

    public String getDataGatherCode() {
        return dataGatherCode;
    }

    public void setDataGatherCode(String dataGatherCode) {
        this.dataGatherCode = dataGatherCode;
    }

    @Override
    public String toString() {
        return "OverDataVo{" +
                "id='" + id + '\'' +
                ", pollutantCode='" + pollutantCode + '\'' +
                ", overTime='" + overTime + '\'' +
                ", dataType='" + dataType + '\'' +
                ", alarmType='" + alarmType + '\'' +
                ", alarmLevel='" + alarmLevel + '\'' +
                ", monitorValue='" + monitorValue + '\'' +
                ", dataGatherCode='" + dataGatherCode + '\'' +
                '}';
    }
}
