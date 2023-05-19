package com.tjpu.sp.model.common.mongodb;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Component
@Document(collection = "RealTimeData")
public class RealTimeDataVO {

    @Id
    private String id;
    @Field("DataGatherCode")
    private String dataGatherCode;

    @Field("DataType")
    private String dataType;

    @Field("MonitorTime")
    private Date monitorTime;

    @Field("RealDataList")
    private List<Map<String,Object>> realDataList;

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

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Date getMonitorTime() {
        return monitorTime;
    }

    public void setMonitorTime(Date monitorTime) {
        this.monitorTime = monitorTime;
    }

    public List<Map<String, Object>> getRealDataList() {
        return realDataList;
    }

    public void setRealDataList(List<Map<String, Object>> realDataList) {
        this.realDataList = realDataList;
    }
}
