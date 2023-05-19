package com.tjpu.sp.model.common.mongodb;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

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
public class OverDataVO {

    @Id
    private String id;
    @Field("PollutantCode")
    private String pollutantCode;
    @Field("OverTime")
    private String overTime;
    @Field("DataType")
    private String dataType;
    @Field("AlarmType")
    private String alarmType;
    @Field("AlarmLevel")
    private String alarmLevel;
    @Field("MonitorValue")
    private String monitorValue;
    @Field("DataGatherCode")
    private String dataGatherCode;



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

    public String getOverTime() {
        return overTime;
    }

    public void setOverTime(String overTime) {
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

    public String getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(String alarmLevel) {
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
