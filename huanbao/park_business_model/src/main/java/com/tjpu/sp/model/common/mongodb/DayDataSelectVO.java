package com.tjpu.sp.model.common.mongodb;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Component
@Document(collection = "DayData")
public class DayDataSelectVO {

    @Id
    private String id;
    @Field("DataGatherCode")
    private String dataGatherCode;
    @Field("MonitorTime")
    private String monitorTime;
    @Field("DataType")
    private String dataType;
    @Field("WaterLevel")
    private String waterlevel;
    @Field("DayDataList")
    private List<Map<String,Object>> dayDataList;

    private Object extdata;

    public Object getExtdata() {
        return extdata;
    }

    public void setExtdata(Object extdata) {
        this.extdata = extdata;
    }

    public String getWaterlevel() {
        return waterlevel;
    }

    public void setWaterlevel(String waterlevel) {
        this.waterlevel = waterlevel;
    }

    public List<Map<String, Object>> getDayDataList() {
        return dayDataList;
    }

    public void setDayDataList(List<Map<String, Object>> dayDataList) {
        this.dayDataList = dayDataList;
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


}
