package com.tjpu.sp.model.common.mongodb;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class StationDayAQIDataVO {

    @Id
    private String id;
    @Field("StationCode")
    private String stationCode;
    @Field("MonitorTime")
    private String monitorTime;
    @Field("AQI")
    private Double aQi;
    @Field("PrimaryPollutant")
    private String primaryPollutant;
    @Field("AirQuality")
    private String airQuality;
    @Field("AirLevel")
    private String airLevel;
    @Field("DataList")
    private List<Map<String,Object>> dataList;
    @Field("DataType")
    private String dataType;

    private Object monitorPointName;

    public Object getMonitorPointName() {
        return monitorPointName;
    }

    public void setMonitorPointName(Object monitorPointName) {
        this.monitorPointName = monitorPointName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    public String getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(String monitorTime) {
        this.monitorTime = monitorTime;
    }

    public Double getaQi() {
        return aQi;
    }

    public void setaQi(Double aQi) {
        this.aQi = aQi;
    }

    public String getPrimaryPollutant() {
        return primaryPollutant;
    }

    public void setPrimaryPollutant(String primaryPollutant) {
        this.primaryPollutant = primaryPollutant;
    }

    public String getAirQuality() {
        return airQuality;
    }

    public void setAirQuality(String airQuality) {
        this.airQuality = airQuality;
    }

    public String getAirLevel() {
        return airLevel;
    }

    public void setAirLevel(String airLevel) {
        this.airLevel = airLevel;
    }

    public List<Map<String, Object>> getDataList() {
        return dataList;
    }

    public void setDataList(List<Map<String, Object>> dataList) {
        this.dataList = dataList;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
}
