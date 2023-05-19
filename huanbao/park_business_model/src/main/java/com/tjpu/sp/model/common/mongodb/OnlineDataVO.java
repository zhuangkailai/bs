package com.tjpu.sp.model.common.mongodb;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
public class OnlineDataVO {

    @Id
    private String id;
    @Field("DataGatherCode")
    private String dataGatherCode;
    @Field("MonitorTime")
    private String monitorTime;
    @Field("DataType")
    private String dataType;
    @Field("DayDataList")
    private List<Map<String,Object>> dayDataList;
    @Field("HourDataList")
    private List<Map<String,Object>> hourDataList;
    @Field("MinuteDataList")
    private List<Map<String,Object>> minuteDataList;
    @Field("RealDataList")
    private List<Map<String,Object>> realDataList;
    private String pollutionname;
    private String outputname;

    public String getPollutionname() {
        return pollutionname;
    }

    public void setPollutionname(String pollutionname) {
        this.pollutionname = pollutionname;
    }

    public String getOutputname() {
        return outputname;
    }

    public void setOutputname(String outputname) {
        this.outputname = outputname;
    }

    public List<Map<String, Object>> getMinuteDataList() {
        return minuteDataList;
    }

    public void setMinuteDataList(List<Map<String, Object>> minuteDataList) {
        this.minuteDataList = minuteDataList;
    }

    public List<Map<String, Object>> getRealDataList() {
        return realDataList;
    }

    public void setRealDataList(List<Map<String, Object>> realDataList) {
        this.realDataList = realDataList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataGatherCode() {
        return dataGatherCode;
    }

    public void setDataGatherCode(String dataGatherCode) {
        this.dataGatherCode = dataGatherCode;
    }

    public String getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(String monitorTime) {
        this.monitorTime = monitorTime;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public List<Map<String, Object>> getDayDataList() {
        return dayDataList;
    }

    public void setDayDataList(List<Map<String, Object>> dayDataList) {
        this.dayDataList = dayDataList;
    }

    public List<Map<String, Object>> getHourDataList() {
        return hourDataList;
    }

    public void setHourDataList(List<Map<String, Object>> hourDataList) {
        this.hourDataList = hourDataList;
    }
}
